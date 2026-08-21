package com.algoschool.submission.checker;

import com.algoschool.problem.entity.CodeProblem;
import com.algoschool.problem.entity.Problem;
import com.algoschool.submission.entity.SubmissionStatus;
import org.springframework.stereotype.Component;

@Component
public class CodeProblemChecker implements ProblemChecker {

    @Override
    public boolean supports(Problem problem) {
        return problem instanceof CodeProblem;
    }

    @Override
    public SubmissionStatus check(Problem problem, String payload) {
        // Вердикт приходит асинхронно от Ejudge: код принят платформой и
        // поставлен в очередь, окончательный статус проставит EjudgePoller.
        return SubmissionStatus.PENDING;
    }
}
