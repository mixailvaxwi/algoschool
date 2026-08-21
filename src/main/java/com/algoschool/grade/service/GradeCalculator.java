package com.algoschool.grade.service;

import com.algoschool.grade.entity.Grade;
import com.algoschool.grade.entity.GradeItem;
import com.algoschool.grade.entity.GradeItemKind;
import com.algoschool.grade.repository.GradeRepository;
import com.algoschool.submission.entity.Submission;
import com.algoschool.submission.repository.SubmissionRepository;
import com.algoschool.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Пересчёт зачтённого балла из истории попыток.
 * <p>
 * Единственное место, где решения превращаются в оценку: пересчёт запускают и
 * отправка решения, и вердикт Ejudge, и зачисление на курс, и смена политики
 * элемента — расходиться эти пути не должны.
 */
@Component
@RequiredArgsConstructor
public class GradeCalculator {

    private final GradeRepository gradeRepository;
    private final SubmissionRepository submissionRepository;

    /** Пересчитывает оценки по одной задаче — обычно после отправки решения. */
    public void recompute(User user, Long problemId, List<GradeItem> items) {
        if (items.isEmpty()) {
            return;
        }
        List<Double> fractions = toFractions(submissionRepository.findScoredAttempts(user.getId(), problemId));
        items.forEach(item -> apply(item, user, fractions));
    }

    /**
     * Пересчитывает оценки сразу по многим задачам одного курса — при
     * зачислении на курс и при смене политики. Попытки достаются одним
     * запросом: иначе курс из полусотни задач стоил бы полусотни обращений.
     */
    public void recomputeAll(User user, List<GradeItem> items) {
        List<GradeItem> problemItems = items.stream()
                .filter(item -> item.getKind() == GradeItemKind.PROBLEM && item.getStep() != null)
                .toList();
        if (problemItems.isEmpty()) {
            return;
        }

        List<Long> problemIds = problemItems.stream()
                .map(item -> item.getStep().getProblem().getId())
                .distinct()
                .toList();

        Map<Long, List<Double>> byProblem = submissionRepository
                .findScoredAttemptsForProblems(user.getId(), problemIds).stream()
                .collect(Collectors.groupingBy(
                        submission -> submission.getProblem().getId(),
                        Collectors.collectingAndThen(Collectors.toList(), GradeCalculator::toFractions)));

        for (GradeItem item : problemItems) {
            apply(item, user, byProblem.getOrDefault(item.getStep().getProblem().getId(), List.of()));
        }
    }

    /**
     * Записывает зачтённый балл за элемент.
     * <p>
     * Ручную оценку не трогает: её поставил человек, и следующая отправка
     * студента не должна молча стереть его решение.
     */
    private void apply(GradeItem item, User user, List<Double> fractions) {
        Grade grade = gradeRepository.findByGradeItemIdAndUserId(item.getId(), user.getId()).orElse(null);
        if (grade != null && grade.isManual()) {
            return;
        }

        OptionalDouble credited = item.getPolicy().apply(fractions);
        if (credited.isEmpty()) {
            // Засчитывать нечего — например, все попытки ещё на проверке.
            // Оставлять прежнюю оценку в такой ситуации неправильно: она уже
            // ничем не подтверждена.
            if (grade != null) {
                gradeRepository.delete(grade);
            }
            return;
        }

        if (grade == null) {
            grade = new Grade();
            grade.setGradeItem(item);
            grade.setUser(user);
        }
        grade.setScore((int) Math.round(credited.getAsDouble() * item.getMaxScore()));
        grade.setManual(false);
        grade.setComment(null);
        grade.setGradedBy(null);
        grade.setUpdatedAt(LocalDateTime.now());
        gradeRepository.save(grade);
    }

    /**
     * Доля от максимума по каждой попытке.
     * <p>
     * Не баллы: вес элемента в курсе и вес задачи в банке — разные величины, и
     * преподаватель может поменять вес уже после того, как студенты нарешали
     * попыток. Доля такую смену переживает.
     */
    private static List<Double> toFractions(List<Submission> attempts) {
        List<Double> fractions = new ArrayList<>(attempts.size());
        for (Submission attempt : attempts) {
            Integer max = attempt.getMaxScore();
            if (attempt.getScore() == null || max == null || max <= 0) {
                continue;
            }
            fractions.add(attempt.getScore() / (double) max);
        }
        return fractions;
    }
}
