package com.algoschool.step.service;

import com.algoschool.step.dto.StepCreateRequest;
import com.algoschool.step.dto.StepTeacherDto;

import java.util.List;

public interface StepService {
    /** Добавляет шаг в урок. username нужен для проверки авторства курса. */
    StepTeacherDto addStepToLesson(Long lessonId, StepCreateRequest request, String username);

    /** Все шаги урока с полными данными (включая правильные ответы) — для автора курса. */
    List<StepTeacherDto> getStepsForLesson(Long lessonId, String username);

    /** Обновляет шаг. Тип шага изменить нельзя — только его содержимое и позицию. */
    StepTeacherDto updateStep(Long stepId, StepCreateRequest request, String username);

    void deleteStep(Long stepId, String username);
}
