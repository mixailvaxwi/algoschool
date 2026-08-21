package com.algoschool.submission.checker;

import com.algoschool.problem.entity.Problem;
import com.algoschool.problem.entity.TextProblem;
import com.algoschool.submission.entity.SubmissionStatus;
import org.springframework.stereotype.Component;

@Component
public class TextProblemChecker implements ProblemChecker {
    @Override
    public boolean supports(Problem problem) {
        return problem instanceof TextProblem;
    }

    @Override
    public SubmissionStatus check(Problem problem, String payload) {
        TextProblem text = (TextProblem) problem;
        boolean isCorrect = text.getCorrectAnswer().trim().equalsIgnoreCase(payload.trim());

        return isCorrect ? SubmissionStatus.CORRECT : SubmissionStatus.WRONG_ANSWER;
    }
}
