package com.algoschool.submission.checker;

import com.algoschool.step.entity.CodeProblem;
import com.algoschool.step.entity.Step;
import com.algoschool.submission.entity.SubmissionStatus;
import org.springframework.stereotype.Component;

@Component
public class CodeProblemChecker implements StepChecker {

    @Override
    public boolean supports(Step step) {
        return step instanceof CodeProblem;
    }

    @Override
    public SubmissionStatus check(Step step, String payload) {
        // TODO: Интеграция с системой выполнения кода (Judge0 / RabbitMQ -> Worker)
        // Код принят платформой и поставлен в очередь на асинхронную проверку.
        return SubmissionStatus.PENDING;
    }
}