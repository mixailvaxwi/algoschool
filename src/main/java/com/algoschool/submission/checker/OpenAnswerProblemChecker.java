package com.algoschool.submission.checker;

import com.algoschool.problem.entity.OpenAnswerProblem;
import com.algoschool.problem.entity.Problem;
import org.springframework.stereotype.Component;

/**
 * Развёрнутый ответ платформа не проверяет — только принимает и ставит в
 * очередь к преподавателю (UC-T-40).
 */
@Component
public class OpenAnswerProblemChecker implements ProblemChecker {

    @Override
    public boolean supports(Problem problem) {
        return problem instanceof OpenAnswerProblem;
    }

    @Override
    public CheckResult check(Problem problem, String payload) {
        return CheckResult.pendingReview();
    }
}
