package com.algoschool.submission.checker;

import com.algoschool.step.entity.Step;
import com.algoschool.step.entity.TextProblem;
import com.algoschool.submission.entity.SubmissionStatus;
import org.springframework.stereotype.Component;

@Component
public class TextProblemChecker implements StepChecker {
    @Override
    public boolean supports(Step step) {
        return step instanceof TextProblem;
    }

    @Override
    public SubmissionStatus check(Step step, String payload) {
        TextProblem problem = (TextProblem) step;
        boolean isCorrect = problem.getCorrectAnswer().trim().equalsIgnoreCase(payload.trim());

        return isCorrect ? SubmissionStatus.CORRECT : SubmissionStatus.WRONG_ANSWER;
    }
}