package com.algoschool.submission.service;

import com.algoschool.exception.AppException;

import com.algoschool.course.service.CourseAccessService;
import com.algoschool.step.entity.Problem;
import com.algoschool.step.entity.Step;
import com.algoschool.step.repository.StepRepository;
import com.algoschool.submission.checker.StepChecker;
import com.algoschool.submission.dto.AssessmentResult;
import com.algoschool.submission.dto.SubmissionRequest;
import com.algoschool.submission.entity.Submission;
import com.algoschool.submission.entity.SubmissionStatus;
import com.algoschool.submission.entity.UserStepProgress;
import com.algoschool.step.repository.ProblemRepository;
import com.algoschool.submission.repository.SubmissionRepository;
import com.algoschool.submission.repository.UserStepProgressRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.algoschool.submission.dto.SubmissionHistoryDto;
import com.algoschool.submission.dto.teacher.TeacherSubmissionDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import com.algoschool.ejudge.EjudgeClient;
import com.algoschool.ejudge.EjudgeUnavailableException;
import com.algoschool.ejudge.EjudgeVerdictMapper;
import com.algoschool.submission.dto.PendingRun;
import com.algoschool.ejudge.dto.EjudgeRunStatusResponse;
import com.algoschool.step.entity.CodeProblem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final StepRepository stepRepository;
    private final UserRepository userRepository;
    private final UserStepProgressRepository progressRepository;
    private final List<StepChecker> checkers;
    private final EjudgeClient ejudgeClient;
    private final CourseAccessService courseAccess;
    private final ProblemRepository problemRepository;
    private final EjudgeVerdictMapper verdictMapper;

    @Transactional
    public AssessmentResult processSubmission(Long stepId, SubmissionRequest request, String username) {
        // Решать задачи можно только на курсе, на который записан.
        courseAccess.requireEnrolledForStep(stepId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));

        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> AppException.notFound("Шаг не найден"));

        if (!(step instanceof Problem problem)) {
            throw AppException.badRequest("Этот шаг является теорией и не требует отправки решения");
        }

        // Счётчик называется attempted_students_count — считаем людей, а не попытки,
        // поэтому проверяем наличие прежних решений ДО сохранения текущего.
        boolean firstAttempt = !submissionRepository.existsByUserIdAndProblemId(user.getId(), problem.getId());

        StepChecker activeChecker = checkers.stream()
                .filter(checker -> checker.supports(problem))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Нет чекера для типа задачи " + problem.getClass().getSimpleName()));

        SubmissionStatus status = activeChecker.check(problem, request.getPayload());

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .payload(request.getPayload())
                .status(status)
                .build();

        if (problem instanceof CodeProblem codeProblem) {
            try {
                Integer runId = ejudgeClient.submitRun(
                        codeProblem.getEjudgeContestId(),
                        codeProblem.getEjudgeProblemId(),
                        codeProblem.getAllowedLanguages(),
                        request.getPayload()
                );
                submission.setExternalRunId(runId);
            } catch (EjudgeUnavailableException e) {
                // Недоступность проверяющей системы — не вердикт по коду студента.
                // Раньше здесь ставился RUNTIME_ERROR, и студент видел «ошибка
                // выполнения» там, где на самом деле лежал Ejudge.
                status = SubmissionStatus.SUBMISSION_FAILED;
                submission.setStatus(status);
                log.error("Решение не передано в Ejudge: {}", e.getMessage());
            }
        }

        submissionRepository.save(submission);

        if (firstAttempt) {
            problemRepository.incrementAttemptedCount(problem.getId());
        }

        if (status == SubmissionStatus.CORRECT) {
            updateUserProgress(user, problem, request.getPayload());
        }

        // Один словарь вердиктов на оба эндпоинта: /submit и /submit/history
        // отдают одно и то же имя статуса. Раньше первый возвращал ACCEPTED,
        // а второй CORRECT для одного и того же исхода.
        String message = getMessageForStatus(status);

        return AssessmentResult.builder()
                .submissionId(submission.getId())
                .status(status.name())
                .message(message)
                .compilerOutput(submission.getCompilerOutput())
                .testResultsJson(submission.getTestResultsJson())
                .build();
    }

    /**
     * Решения, ожидающие вердикта. Отдаём только идентификаторы: опрос ходит
     * по сети вне транзакции, и сущности там были бы отсоединены.
     */
    @Transactional(readOnly = true)
    public List<PendingRun> findPendingRuns() {
        return submissionRepository.findByStatusWithProblem(SubmissionStatus.PENDING).stream()
                .filter(sub -> sub.getExternalRunId() != null)
                .filter(sub -> sub.getProblem() instanceof CodeProblem)
                .map(sub -> new PendingRun(
                        sub.getId(),
                        ((CodeProblem) sub.getProblem()).getEjudgeContestId(),
                        sub.getExternalRunId()))
                .toList();
    }

    /**
     * Записывает вердикт Ejudge. Отдельная короткая транзакция: сетевой вызов
     * уже сделан вызывающим (EjudgePoller) и в неё не попадает.
     */
    @Transactional
    public void applyEjudgeVerdict(Long submissionId, EjudgeRunStatusResponse response) {
        Integer ejudgeStatus = response.getResult().getRun() != null
                ? response.getResult().getRun().getStatus()
                : null;

        SubmissionStatus mapped = verdictMapper.toFinalStatus(ejudgeStatus).orElse(null);
        if (mapped == null) {
            return; // проверка ещё идёт либо код вердикта незнаком — спросим позже
        }

        Submission sub = submissionRepository.findById(submissionId).orElse(null);
        if (sub == null || sub.getStatus() != SubmissionStatus.PENDING) {
            return; // решение удалили или вердикт уже записали
        }

        sub.setStatus(mapped);
        if (response.getResult().getCompilerOutput() != null) {
            sub.setCompilerOutput(response.getResult().getCompilerOutput());
        }
        if (response.getResult().getTests() != null) {
            sub.setTestResultsJson(response.getResult().getTests().toString());
        }
        submissionRepository.save(sub);

        if (mapped == SubmissionStatus.CORRECT && sub.getProblem() instanceof Problem problem) {
            updateUserProgress(sub.getUser(), problem, sub.getPayload());
        }
        log.info("Решение {} проверено Ejudge: {}", submissionId, mapped);
    }

    // Вспомогательный метод для генерации текстов
    private String getMessageForStatus(SubmissionStatus status) {
        return switch (status) {
            case CORRECT -> "Отличное решение! Вы справились.";
            case WRONG_ANSWER -> "Ответ неверный. Попробуйте еще раз.";
            case PENDING -> "Решение отправлено на проверку (может занять некоторое время).";
            case COMPILATION_ERROR -> "Ошибка компиляции кода.";
            case SUBMISSION_FAILED -> "Проверяющая система недоступна. Решение сохранено, попробуйте отправить его позже.";
            default -> "Произошла ошибка при выполнении.";
        };
    }

    // ... метод updateUserProgress остается без изменений ...
    private void updateUserProgress(User user, Problem problem, String payload) {
        UserStepProgress progress = progressRepository.findByUserAndStep(user, problem).orElse(null);

        // Успех засчитывается только в первый раз: раньше счётчик рос при каждом
        // верном решении, и повторная отправка того же ответа накручивала статистику.
        boolean firstSuccess = (progress == null) || !progress.isCompleted();

        if (progress == null) {
            progress = UserStepProgress.builder()
                    .user(user)
                    .step(problem)
                    .build();
        }

        progress.setCompleted(true);
        progress.setSubmittedPayload(payload);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);

        if (firstSuccess) {
            // Атомарный UPDATE вместо чтения-записи через сущность: параллельные
            // отправки больше не затирают инкремент друг друга.
            problemRepository.incrementSuccessCount(problem.getId());
        }
    }


    @Transactional(readOnly = true)
    public List<SubmissionHistoryDto> getStudentHistory(Long stepId, String username) {
        courseAccess.requireEnrolledForStep(stepId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));

        return submissionRepository.findAllByUserIdAndProblemIdOrderByCreatedAtDesc(user.getId(), stepId)
                .stream()
                .map(sub -> new SubmissionHistoryDto(
                        sub.getId(),
                        sub.getPayload(),
                        sub.getStatus(),
                        sub.getCreatedAt(),
                        sub.getCompilerOutput(),
                        sub.getTestResultsJson()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeacherSubmissionDto> getTeacherSubmissions(Long courseId, Long studentId, SubmissionStatus status,
                                                           String teacherUsername) {
        // courseId обязателен и обязан принадлежать этому преподавателю. Раньше
        // без него метод отдавал решения (включая исходный код) всех студентов
        // платформы любому авторизованному пользователю.
        courseAccess.requireAuthor(courseId, teacherUsername);

        Specification<Submission> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Фильтр по курсу (джоинимся до курса через problem -> lesson -> module -> course)
            predicates.add(cb.equal(root.get("problem").get("lesson").get("module").get("course").get("id"), courseId));
            // Фильтр по студенту
            if (studentId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), studentId));
            }
            // Фильтр по статусу
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Сортируем от новых к старым
        return submissionRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(sub -> new TeacherSubmissionDto(
                        sub.getId(),
                        sub.getUser().getId(),
                        sub.getUser().getName() != null ? sub.getUser().getName() : sub.getUser().getUsername(),
                        sub.getProblem().getLesson().getModule().getCourse().getId(),
                        sub.getProblem().getLesson().getModule().getCourse().getTitle(),
                        sub.getProblem().getLesson().getModule().getPositionIndex(),
                        sub.getProblem().getLesson().getOrderIndex(),
                        sub.getProblem().getOrderIndex(),
                        sub.getPayload(),
                        sub.getStatus(),
                        sub.getCreatedAt()
                )).toList();
    }
}