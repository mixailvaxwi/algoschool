package com.algoschool.submission.checker;

import com.algoschool.problem.entity.ChoiceProblem;
import com.algoschool.problem.entity.Problem;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ChoiceProblemChecker implements ProblemChecker {
    @Override
    public boolean supports(Problem problem) {
        return problem instanceof ChoiceProblem;
    }

    @Override
    public CheckResult check(Problem problem, String payload) {
        ChoiceProblem choice = (ChoiceProblem) problem;

        // Ответ — либо один индекс ("2"), либо несколько через запятую ("0,2")
        // для isMultipleChoice. Зачёт — только полное совпадение множеств:
        // отметить лишний вариант или пропустить один из верных — неверный ответ.
        //
        // Частичного балла здесь намеренно нет, в отличие от соответствия и
        // упорядочивания: там неверный ответ стоит студенту вариантов, а тут
        // стратегия «отметить всё» приносила бы очки за незнание.
        Set<Integer> studentAnswer;
        try {
            studentAnswer = Stream.of(payload.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
        } catch (NumberFormatException e) {
            return CheckResult.wrong();
        }

        boolean isCorrect = !studentAnswer.isEmpty()
                && studentAnswer.equals(choice.getCorrectOptionIndexes());

        return isCorrect ? CheckResult.correct(choice.getMaxScore()) : CheckResult.wrong();
    }
}
