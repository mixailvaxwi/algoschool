package com.algoschool.submission.checker;

import com.algoschool.step.entity.ChoiceProblem;
import com.algoschool.step.entity.Step;
import com.algoschool.submission.entity.SubmissionStatus;
import org.springframework.stereotype.Component;

@Component
public class ChoiceProblemChecker implements StepChecker {
    @Override
    public boolean supports(Step step) {
        return step instanceof ChoiceProblem;
    }

    @Override
    public SubmissionStatus check(Step step, String payload) {
        ChoiceProblem problem = (ChoiceProblem) step;
        try {
            int studentAnswerIdx = Integer.parseInt(payload.trim());
            boolean isCorrect = problem.getCorrectOptionIndex() == studentAnswerIdx;

            return isCorrect ? SubmissionStatus.CORRECT : SubmissionStatus.WRONG_ANSWER;
        } catch (NumberFormatException e) {
            return SubmissionStatus.WRONG_ANSWER;
        }
    }
}