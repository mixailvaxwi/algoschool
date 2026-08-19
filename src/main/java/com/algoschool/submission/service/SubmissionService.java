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
import com.algoschool.ejudge.dto.EjudgeRunStatusResponse;
import com.algoschool.step.entity.CodeProblem;
import org.springframework.scheduling.annotation.Scheduled;
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
                // Временно хардкодим "3" (Java в Ejudge). Позже можно брать из UI.
                Integer runId = ejudgeClient.submitRun(
                        codeProblem.getEjudgeContestId(),
                        codeProblem.getEjudgeProblemId(),
                        "3",
                        request.getPayload()
                );
                submission.setExternalRunId(runId);
            } catch (Exception e) {
                submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                log.error("Не удалось отправить код в Ejudge", e);
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

    @Scheduled(fixedDelay = 3000) // Опрашивать раз в 3 секунды
    @Transactional
    public void pollEjudgeStatuses() {
        List<Submission> pendingSubmissions = submissionRepository.findByStatusWithProblem(SubmissionStatus.PENDING);

        for (Submission sub : pendingSubmissions) {
            if (sub.getExternalRunId() == null || !(sub.getProblem() instanceof CodeProblem problem)) continue;

            try {
                // ДОБАВЛЯЕМ ЛОГ, ЧТОБЫ ВИДЕТЬ, ЧТО ЦИКЛ РАБОТАЕТ
                log.info("Спрашиваем статус у Ejudge для run_id: {}", sub.getExternalRunId());

                EjudgeRunStatusResponse statusResponse = ejudgeClient.getRunStatus(problem.getEjudgeContestId(), sub.getExternalRunId());

                if (statusResponse != null && statusResponse.isOk() && statusResponse.getResult() != null) {
                    Integer ejudgeStatus = statusResponse.getResult().getRun().getStatus();

                    // В Ejudge статусы 11 (Pending) и 16 (Pending Review) означают, что проверка идет
                    // Статусы <= 95 (обычно 0-10) — это финальные вердикты
                    if (ejudgeStatus <= 95 && ejudgeStatus != 11 && ejudgeStatus != 16) {
                        SubmissionStatus mappedStatus = mapEjudgeStatus(ejudgeStatus);
                        sub.setStatus(mappedStatus);

                        // --- СОХРАНЯЕМ ЛОГ КОМПИЛЯТОРА ---
                        if (statusResponse.getResult().getCompilerOutput() != null) {
                            sub.setCompilerOutput(statusResponse.getResult().getCompilerOutput());
                        }

                        // --- СОХРАНЯЕМ ТЕСТЫ В ВИДЕ JSON-СТРОКИ ---
                        if (statusResponse.getResult().getTests() != null) {
                            sub.setTestResultsJson(statusResponse.getResult().getTests().toString());
                        }

                        submissionRepository.save(sub);

                        // Если ответ верный, начисляем прогресс через твой же метод!
                        if (mappedStatus == SubmissionStatus.CORRECT) {
                            updateUserProgress(sub.getUser(), problem, sub.getPayload());
                        }
                        log.info("Решение {} проверено Ejudge. Новый статус: {}", sub.getId(), mappedStatus);
                    }
                }
            } catch (Exception e) {
                log.error("Ошибка при опросе решения {}", sub.getId(), e);
            }
        }
    }

    private SubmissionStatus mapEjudgeStatus(Integer ejudgeStatus) {
        return switch (ejudgeStatus) {
            case 0 -> SubmissionStatus.CORRECT; // OK
            case 1 -> SubmissionStatus.COMPILATION_ERROR; // CE
            case 2 -> SubmissionStatus.RUNTIME_ERROR; // RE
            case 3 -> SubmissionStatus.TIME_LIMIT_EXCEEDED; // TL
            case 12 -> SubmissionStatus.MEMORY_LIMIT_EXCEEDED; // ML
            default -> SubmissionStatus.WRONG_ANSWER; // WA и прочие ошибки
        };
    }

    // Вспомогательный метод для генерации текстов
    private String getMessageForStatus(SubmissionStatus status) {
        return switch (status) {
            case CORRECT -> "Отличное решение! Вы справились.";
            case WRONG_ANSWER -> "Ответ неверный. Попробуйте еще раз.";
            case PENDING -> "Решение отправлено на проверку (может занять некоторое время).";
            case COMPILATION_ERROR -> "Ошибка компиляции кода.";
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