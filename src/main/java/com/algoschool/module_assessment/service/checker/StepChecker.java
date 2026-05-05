package com.algoschool.module_assessment.service.checker;
import com.algoschool.module_assessment.entity.Step;

public interface StepChecker {
    boolean supports(Step step);
    boolean check(Step step, String userPayload);
}