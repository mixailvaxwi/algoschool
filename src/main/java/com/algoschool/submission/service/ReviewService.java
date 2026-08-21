package com.algoschool.submission.service;

import com.algoschool.course.service.CourseAccessService;
import com.algoschool.exception.AppException;
import com.algoschool.grade.service.GradeService;
import com.algoschool.problem.entity.OpenAnswerProblem;
import com.algoschool.problem.entity.Problem;
import com.algoschool.step.entity.ProblemStep;
import com.algoschool.submission.dto.teacher.ReviewItemDto;
import com.algoschool.submission.dto.teacher.ReviewRequest;
import com.algoschool.submission.entity.Submission;
import com.algoschool.submission.entity.SubmissionStatus;
import com.algoschool.submission.repository.SubmissionRepository;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ручная проверка развёрнутых ответов (UC-T-40, UC-T-41).
 * <p>
 * Отдельный сервис, а не метод в SubmissionService: у проверки другой актор
 * (преподаватель, а не студент) и другие права — здесь всюду нужен автор курса,
 * а не зачисление на него.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final CourseAccessService courseAccess;
    private final GradeService gradeService;
    private final SubmissionService submissionService;

    /**
     * Очередь проверки по курсу — от самых старых ответов к свежим: студент,
     * сдавший раньше, и ответа ждёт дольше.
     */
    @Transactional(readOnly = true)
    public List<ReviewItemDto> queue(Long courseId, String teacherUsername) {
        courseAccess.requireAuthor(courseId, teacherUsername);

        return submissionRepository.findForReview(courseId).stream()
                .map(this::toReviewItem)
                .toList();
    }

    @Transactional
    public void review(Long submissionId, ReviewRequest request, String teacherUsername) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> AppException.notFound("Решение не найдено"));

        ProblemStep step = submission.getStep();
        if (step == null) {
            // Задачу сняли с урока, пока ответ ждал проверки: проверять больше
            // некому — курс, к которому это относилось, задачу не содержит.
            throw AppException.conflict("Задача снята с урока — проверять это решение больше некому");
        }
        courseAccess.requireAuthor(step.getLesson().getModule().getCourse().getId(), teacherUsername);

        if (submission.getStatus() != SubmissionStatus.PENDING_REVIEW) {
            throw AppException.conflict("Это решение уже проверено");
        }
        if (request.getComment() == null || request.getComment().isBlank()) {
            throw AppException.badRequest("Напишите студенту, за что выставлен балл: комментарий обязателен");
        }

        Problem problem = submission.getProblem();
        int maxScore = problem.getMaxScore();
        if (request.getScore() > maxScore) {
            throw AppException.badRequest("Балл больше веса задачи (" + maxScore + ")");
        }

        User reviewer = userRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> AppException.unauthorized("Требуется вход в систему"));

        submission.setScore(request.getScore());
        submission.setMaxScore(maxScore);
        submission.setStatus(statusFor(request.getScore(), maxScore));
        submission.setReviewedBy(reviewer);
        submission.setReviewedAt(LocalDateTime.now());
        submission.setReviewComment(request.getComment());
        submissionRepository.save(submission);

        // Полный балл — задача решена: отмечаем прогресс так же, как это делает
        // автоматическая проверка, иначе шаг остался бы непройденным.
        if (submission.getStatus() == SubmissionStatus.CORRECT) {
            submissionService.markSolvedAfterReview(submission);
        }
        gradeService.recomputeForProblem(submission.getUser(), problem);

        log.info("Решение {} проверено преподавателем {}: {} из {}",
                submissionId, teacherUsername, request.getScore(), maxScore);
    }

    /**
     * Вердикт по выставленному баллу: полный балл — верно, ноль — неверно,
     * между ними — частично. Отдельного «проверено вручную» не заводим:
     * студенту важен исход, а не то, кто его определил.
     */
    private SubmissionStatus statusFor(int score, int maxScore) {
        if (score >= maxScore) {
            return SubmissionStatus.CORRECT;
        }
        return score <= 0 ? SubmissionStatus.WRONG_ANSWER : SubmissionStatus.PARTIALLY_CORRECT;
    }

    private ReviewItemDto toReviewItem(Submission submission) {
        ProblemStep step = submission.getStep();
        Problem problem = submission.getProblem();

        return new ReviewItemDto(
                submission.getId(),
                submission.getUser().getId(),
                displayName(submission.getUser()),
                step.getLesson().getModule().getCourse().getId(),
                step.getLesson().getModule().getCourse().getTitle(),
                step.getLesson().getId(),
                step.getLesson().getTitle(),
                step.getId(),
                problem.getTitle(),
                problem.getDescription(),
                problem instanceof OpenAnswerProblem open ? open.getReviewGuidelines() : null,
                submission.getPayload(),
                problem.getMaxScore(),
                submission.getCreatedAt());
    }

    private String displayName(User user) {
        return user.getName() == null || user.getName().isBlank() ? user.getUsername() : user.getName();
    }
}
