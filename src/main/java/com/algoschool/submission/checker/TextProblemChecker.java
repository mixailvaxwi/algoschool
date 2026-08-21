package com.algoschool.submission.checker;

import com.algoschool.problem.entity.Problem;
import com.algoschool.problem.entity.TextProblem;
import org.springframework.stereotype.Component;

@Component
public class TextProblemChecker implements ProblemChecker {
    @Override
    public boolean supports(Problem problem) {
        return problem instanceof TextProblem;
    }

    @Override
    public CheckResult check(Problem problem, String payload) {
        TextProblem text = (TextProblem) problem;
        boolean isCorrect = text.getCorrectAnswer().trim().equalsIgnoreCase(payload.trim());

        return isCorrect ? CheckResult.correct(text.getMaxScore()) : CheckResult.wrong();
    }
}
