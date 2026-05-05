package com.algoschool.module_assessment.service.checker;

import com.algoschool.module_assessment.entity.Step;
import com.algoschool.module_assessment.entity.Theory;
import org.springframework.stereotype.Component;

@Component
public class TheoryChecker implements StepChecker {
    @Override
    public boolean supports(Step step) { return step instanceof Theory; }

    @Override
    public boolean check(Step step, String userPayload) {
        return true; // Теорию нельзя "завалить"
    }
}