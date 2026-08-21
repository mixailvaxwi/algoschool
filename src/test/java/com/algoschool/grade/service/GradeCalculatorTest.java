package com.algoschool.grade.service;

import com.algoschool.grade.entity.Grade;
import com.algoschool.grade.entity.GradeItem;
import com.algoschool.grade.entity.GradeItemKind;
import com.algoschool.grade.entity.GradePolicy;
import com.algoschool.grade.repository.GradeRepository;
import com.algoschool.problem.entity.TextProblem;
import com.algoschool.step.entity.ProblemStep;
import com.algoschool.submission.entity.Submission;
import com.algoschool.submission.repository.SubmissionRepository;
import com.algoschool.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Превращение истории попыток в зачтённый балл — то место, где сходятся
 * политика зачёта, вес элемента и ручные оценки.
 */
@ExtendWith(MockitoExtension.class)
class GradeCalculatorTest {

    @Mock private GradeRepository gradeRepository;
    @Mock private SubmissionRepository submissionRepository;

    private GradeCalculator calculator;
    private User student;

    @BeforeEach
    void setUp() {
        calculator = new GradeCalculator(gradeRepository, submissionRepository);
        student = new User();
        student.setId(7L);
    }

    private GradeItem item(int maxScore, GradePolicy policy) {
        TextProblem problem = new TextProblem();
        problem.setId(100L);

        ProblemStep step = new ProblemStep();
        step.setProblem(problem);

        GradeItem gradeItem = new GradeItem();
        gradeItem.setId(1L);
        gradeItem.setKind(GradeItemKind.PROBLEM);
        gradeItem.setStep(step);
        gradeItem.setMaxScore(maxScore);
        gradeItem.setPolicy(policy);
        return gradeItem;
    }

    private Submission attempt(Integer score, Integer maxScore) {
        Submission submission = new Submission();
        submission.setScore(score);
        submission.setMaxScore(maxScore);
        return submission;
    }

    private Grade capturedGrade() {
        ArgumentCaptor<Grade> captor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void creditsBestAttempt() {
        when(submissionRepository.findScoredAttempts(7L, 100L))
                .thenReturn(List.of(attempt(0, 1), attempt(1, 1)));
        when(gradeRepository.findByGradeItemIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        calculator.recompute(student, 100L, List.of(item(1, GradePolicy.BEST)));

        assertThat(capturedGrade().getScore()).isEqualTo(1);
    }

    @Test
    void lastAttemptCanLowerTheGrade() {
        when(submissionRepository.findScoredAttempts(7L, 100L))
                .thenReturn(List.of(attempt(1, 1), attempt(0, 1)));
        when(gradeRepository.findByGradeItemIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        calculator.recompute(student, 100L, List.of(item(1, GradePolicy.LAST)));

        assertThat(capturedGrade().getScore()).isZero();
    }

    /**
     * Задача в банке весит 1 балл, а в этом курсе — 5. Балл попытки пересчитан
     * пропорционально, иначе вес элемента ничего бы не значил.
     */
    @Test
    void rescalesAttemptToItemWeight() {
        when(submissionRepository.findScoredAttempts(7L, 100L)).thenReturn(List.of(attempt(1, 1)));
        when(gradeRepository.findByGradeItemIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        calculator.recompute(student, 100L, List.of(item(5, GradePolicy.BEST)));

        assertThat(capturedGrade().getScore()).isEqualTo(5);
    }

    /** Ручную оценку поставил человек — пересчёт по решениям её не трогает. */
    @Test
    void leavesManualGradeAlone() {
        Grade manual = new Grade();
        manual.setScore(3);
        manual.setManual(true);
        when(gradeRepository.findByGradeItemIdAndUserId(1L, 7L)).thenReturn(Optional.of(manual));

        calculator.recompute(student, 100L, List.of(item(5, GradePolicy.BEST)));

        verify(gradeRepository, never()).save(any());
        verify(gradeRepository, never()).delete(any());
        // Историю попыток при этом даже не читаем: она ни на что не влияет.
        verify(submissionRepository).findScoredAttempts(anyLong(), anyLong());
        assertThat(manual.getScore()).isEqualTo(3);
    }

    /**
     * Непроверенные попытки в зачёт не идут: PENDING ещё проверяется, а
     * SUBMISSION_FAILED — это сбой Ejudge, а не вердикт по ответу. Обе приходят
     * со score = null.
     */
    @Test
    void ignoresUnscoredAttempts() {
        when(submissionRepository.findScoredAttempts(7L, 100L))
                .thenReturn(List.of(attempt(null, 1), attempt(1, 1)));
        when(gradeRepository.findByGradeItemIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

        calculator.recompute(student, 100L, List.of(item(1, GradePolicy.LAST)));

        // Последняя ПРОВЕРЕННАЯ попытка — верная, поэтому балл полный,
        // а не ноль от непроверенной.
        assertThat(capturedGrade().getScore()).isEqualTo(1);
    }

    /**
     * Засчитывать нечего — оценки быть не должно. Прочерк в журнале означает
     * «не сдавал», и оставлять там прежний балл, который уже ничем не
     * подтверждён, нельзя.
     */
    @Test
    void removesGradeWhenNothingIsCredited() {
        Grade stale = new Grade();
        stale.setScore(1);
        when(submissionRepository.findScoredAttempts(7L, 100L)).thenReturn(List.of());
        when(gradeRepository.findByGradeItemIdAndUserId(1L, 7L)).thenReturn(Optional.of(stale));

        calculator.recompute(student, 100L, List.of(item(1, GradePolicy.BEST)));

        verify(gradeRepository).delete(stale);
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void doesNothingWithoutItems() {
        calculator.recompute(student, 100L, List.of());

        verifyNoInteractions(submissionRepository, gradeRepository);
    }
}
