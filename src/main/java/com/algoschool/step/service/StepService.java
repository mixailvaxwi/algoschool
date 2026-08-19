package com.algoschool.step.service;

import com.algoschool.step.dto.StepCreateRequest;
import com.algoschool.step.entity.Step;

public interface StepService {
    /** Добавляет шаг в урок. username нужен для проверки авторства курса. */
    Step addStepToLesson(Long lessonId, StepCreateRequest request, String username);
}
