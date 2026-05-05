package com.algoschool.module_assessment.service;

import com.algoschool.module_assessment.dto.AssessmentResult;

public interface AssessmentService {
    // Точное совпадение с тем, что вызывает контроллер!
    AssessmentResult submitSolution(Long userId, Long stepId, String studentAnswer);
}