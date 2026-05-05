package com.algoschool.module_assessment.service.checker;

import com.algoschool.module_assessment.entity.Step;
import com.algoschool.module_assessment.entity.TextProblem;
import org.springframework.stereotype.Component;

@Component
public class TextProblemChecker implements StepChecker {
    @Override
    public boolean supports(Step step) { return step instanceof TextProblem; }

    @Override
    public boolean check(Step step, String userPayload) {
        TextProblem textProblem = (TextProblem) step;
        if (userPayload == null) return false;

        // Удаляем пробелы по краям и не смотрим на регистр
        return userPayload.trim().equalsIgnoreCase(textProblem.getCorrectAnswer().trim());
    }
}