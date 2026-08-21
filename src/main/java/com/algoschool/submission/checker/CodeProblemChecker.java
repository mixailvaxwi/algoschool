package com.algoschool.submission.checker;

import com.algoschool.problem.entity.CodeProblem;
import com.algoschool.problem.entity.Problem;
import org.springframework.stereotype.Component;

@Component
public class CodeProblemChecker implements ProblemChecker {

    @Override
    public boolean supports(Problem problem) {
        return problem instanceof CodeProblem;
    }

    @Override
    public CheckResult check(Problem problem, String payload) {
        // Вердикт приходит асинхронно от Ejudge: код принят платформой и
        // поставлен в очередь, окончательный статус проставит EjudgePoller.
        return CheckResult.pending();
    }
}
