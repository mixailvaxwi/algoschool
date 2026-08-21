package com.algoschool.submission.service;

import com.algoschool.course.service.CourseAccessService;
import com.algoschool.ejudge.EjudgeClient;
import com.algoschool.ejudge.EjudgeUnavailableException;
import com.algoschool.ejudge.EjudgeVerdictMapper;
import com.algoschool.ejudge.dto.EjudgeRunStatusResponse;
import com.algoschool.exception.AppException;
import com.algoschool.grade.service.GradeService;
import com.algoschool.problem.entity.CodeProblem;
import com.algoschool.problem.entity.Problem;
import com.algoschool.problem.repository.ProblemRepository;
import com.algoschool.step.entity.ProblemStep;
import com.algoschool.step.entity.Step;
import com.algoschool.step.repository.ProblemStepRepository;
import com.algoschool.step.repository.StepRepository;
import com.algoschool.submission.checker.ProblemChecker;
import com.algoschool.submission.dto.AssessmentResult;
import com.algoschool.submission.dto.PendingRun;
import com.algoschool.submission.dto.SubmissionHistoryDto;
import com.algoschool.submission.dto.SubmissionRequest;
import com.algoschool.submission.dto.teacher.TeacherSubmissionDto;
import com.algoschool.submission.entity.Submission;
import com.algoschool.submission.entity.SubmissionStatus;
import com.algoschool.submission.entity.UserStepProgress;
import com.algoschool.submission.repository.SubmissionRepository;
import com.algoschool.submission.repository.UserStepProgressRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final StepRepository stepRepository;
    private final ProblemStepRepository problemStepRepository;
    private final UserRepository userRepository;
    private final UserStepProgressRepository progressRepository;
    private final List<ProblemChecker> checkers;
    private final EjudgeClient ejudgeClient;
    private final CourseAccessService courseAccess;
    private final ProblemRepository problemRepository;
    private final EjudgeVerdictMapper verdictMapper;
    private final GradeService gradeService;

    @Transactional
    public AssessmentResult processSubmission(Long stepId, SubmissionRequest request, String username) {
        // Решать задачи можно только на курсе, на который записан.
        courseAccess.requireEnrolledForStep(stepId, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));

        ProblemStep step = requireProblemStep(stepId);
        Problem problem = step.getProblem();

        // Счётчик называется attempted_students_count — считаем людей, а не попытки,
        // поэтому проверяем наличие прежних решений ДО сохранения текущего.
        boolean firstAttempt = !submissionRepository.existsByUserIdAndProblemId(user.getId(), problem.getId());

        ProblemChecker activeChecker = checkers.stream()
                .filter(checker -> checker.supports(problem))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Нет чекера для типа задачи " + problem.getClass().getSimpleName()));

        SubmissionStatus status = activeChecker.check(problem, request.getPayload());

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .step(step)
                .payload(request.getPayload())
                .status(status)
                .maxScore(problem.getMaxScore())
                .score(scoreFor(status, problem))
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
                submission.setScore(null);
                log.error("Решение не передано в Ejudge: {}", e.getMessage());
            }
        }

        submissionRepository.save(submission);

        if (firstAttempt) {
            problemRepository.incrementAttemptedCount(problem.getId());
        }

        if (status == SubmissionStatus.CORRECT) {
            updateUserProgress(user, problem, step, request.getPayload());
        }
        // Пересчитываем и по неверной попытке: политика зачёта может быть
        // «последняя», и тогда неудача после успеха меняет оценку.
        if (submission.getScore() != null) {
            gradeService.recomputeForProblem(user, problem);
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
        sub.setScore(scoreFor(mapped, sub.getProblem()));
        if (sub.getMaxScore() == null) {
            sub.setMaxScore(sub.getProblem().getMaxScore());
        }
        if (response.getResult().getCompilerOutput() != null) {
            sub.setCompilerOutput(response.getResult().getCompilerOutput());
        }
        if (response.getResult().getTests() != null) {
            sub.setTestResultsJson(response.getResult().getTests().toString());
        }
        submissionRepository.save(sub);

        if (mapped == SubmissionStatus.CORRECT) {
            // step может быть null, если задачу успели снять с урока: решение
            // всё равно засчитываем — оно принадлежит задаче, а не размещению.
            updateUserProgress(sub.getUser(), sub.getProblem(), sub.getStep(), sub.getPayload());
        }
        if (sub.getScore() != null) {
            gradeService.recomputeForProblem(sub.getUser(), sub.getProblem());
        }
        log.info("Решение {} проверено Ejudge: {}", submissionId, mapped);
    }

    /**
     * Балл за попытку по вердикту.
     * <p>
     * Пока оценка бинарная: вес задачи целиком или ноль. Частичный балл за
     * подзадачу появится вместе с типами задач, которые его допускают, — здесь
     * важно, что балл уже хранится у отправки, а не выводится из вердикта на лету.
     * <p>
     * null для PENDING и SUBMISSION_FAILED: в первом случае проверка не
     * закончена, во втором лежал Ejudge — это не вердикт по ответу студента и
     * в зачёт идти не должно.
     */
    private Integer scoreFor(SubmissionStatus status, Problem problem) {
        return switch (status) {
            case CORRECT -> problem.getMaxScore();
            case PENDING, SUBMISSION_FAILED -> null;
            default -> 0;
        };
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

    /**
     * Отмечает задачу решённой во всех уроках, где она стоит и куда студент
     * записан.
     * <p>
     * Решение засчитывается задаче, а прогресс живёт на размещении. Пока
     * задача была шагом, это было одно и то же; теперь без обхода размещений
     * студент, решивший задачу в одном уроке, видел бы её непройденной в
     * другом — при том что история попыток там уже показывала верный ответ.
     */
    private void updateUserProgress(User user, Problem problem, ProblemStep submittedAt, String payload) {
        Set<ProblemStep> placements = new LinkedHashSet<>(
                problemStepRepository.findPlacementsInEnrolledCourses(problem.getId(), user.getId()));
        if (submittedAt != null) {
            // Автор курса не «записан» на собственный курс, но свои задачи решать может.
            placements.add(submittedAt);
        }

        LocalDateTime now = LocalDateTime.now();
        for (ProblemStep placement : placements) {
            UserStepProgress progress = progressRepository.findByUserAndStep(user, placement)
                    .orElseGet(() -> UserStepProgress.builder()
                            .user(user)
                            .step(placement)
                            .build());

            progress.setCompleted(true);
            progress.setSubmittedPayload(payload);
            progress.setCompletedAt(now);
            progressRepository.save(progress);
        }

        // Успех засчитывается один раз на человека. Считаем по решениям, а не
        // по прогрессу: прогресс появляется у каждого размещения отдельно, и
        // второй урок с той же задачей накрутил бы счётчик. Текущее решение
        // уже сохранено обоими вызывающими, поэтому первое успешное — ровно
        // одно в истории.
        long correctSubmissions = submissionRepository.countByUserIdAndProblemIdAndStatus(
                user.getId(), problem.getId(), SubmissionStatus.CORRECT);
        if (correctSubmissions == 1) {
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

        // История принадлежит задаче: если та же задача стоит в двух уроках,
        // студент видит в обоих свои попытки целиком, а не половину.
        Long problemId = requireProblemStep(stepId).getProblem().getId();

        return submissionRepository.findAllByUserIdAndProblemIdOrderByCreatedAtDesc(user.getId(), problemId)
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
            // Курс определяется размещением, а не задачей: сама задача живёт в
            // банке и ни к какому курсу не относится. Решения по снятым с урока
            // размещениям (step_id = NULL) в выдачу не попадают — их больше
            // некуда отнести.
            predicates.add(cb.equal(root.get("step").get("lesson").get("module").get("course").get("id"), courseId));
            if (studentId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), studentId));
            }
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
                        sub.getStep().getLesson().getModule().getCourse().getId(),
                        sub.getStep().getLesson().getModule().getCourse().getTitle(),
                        sub.getStep().getLesson().getModule().getPositionIndex(),
                        sub.getStep().getLesson().getOrderIndex(),
                        sub.getStep().getOrderIndex(),
                        sub.getPayload(),
                        sub.getStatus(),
                        sub.getCreatedAt()
                )).toList();
    }

    private ProblemStep requireProblemStep(Long stepId) {
        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> AppException.notFound("Шаг не найден"));

        if (!(step instanceof ProblemStep problemStep)) {
            throw AppException.badRequest("Этот шаг является теорией и не требует отправки решения");
        }
        return problemStep;
    }
}
