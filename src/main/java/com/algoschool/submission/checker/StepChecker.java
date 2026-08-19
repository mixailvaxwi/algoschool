package com.algoschool.submission.checker;

import com.algoschool.step.entity.Step;
import com.algoschool.submission.entity.SubmissionStatus;

public interface StepChecker {
    boolean supports(Step step);

    // Теперь возвращаем конкретный статус вместо true/false!
    SubmissionStatus check(Step step, String payload);
}