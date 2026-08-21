package com.algoschool.submission.checker;

import com.algoschool.problem.entity.ChoiceProblem;
import com.algoschool.problem.entity.Problem;
import com.algoschool.submission.entity.SubmissionStatus;
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
    public SubmissionStatus check(Problem problem, String payload) {
        ChoiceProblem choice = (ChoiceProblem) problem;

        // Ответ — либо один индекс ("2"), либо несколько через запятую ("0,2")
        // для isMultipleChoice. Зачёт — только полное совпадение множеств:
        // отметить лишний вариант или пропустить один из верных — неверный ответ.
        Set<Integer> studentAnswer;
        try {
            studentAnswer = Stream.of(payload.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
        } catch (NumberFormatException e) {
            return SubmissionStatus.WRONG_ANSWER;
        }

        boolean isCorrect = !studentAnswer.isEmpty()
                && studentAnswer.equals(choice.getCorrectOptionIndexes());

        return isCorrect ? SubmissionStatus.CORRECT : SubmissionStatus.WRONG_ANSWER;
    }
}
