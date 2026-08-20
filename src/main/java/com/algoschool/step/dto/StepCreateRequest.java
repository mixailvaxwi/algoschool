package com.algoschool.step.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Создание шага урока.
 * <p>
 * Поля специфичны для типа шага, поэтому обязательными помечены только общие:
 * остальное проверяет StepServiceImpl при разборе stepType. Раньше валидации
 * не было вовсе, и пустой запрос доходил до базы.
 */
@Data
public class StepCreateRequest {

    @NotBlank(message = "Тип шага обязателен")
    private String stepType;

    @NotNull(message = "Порядковый номер обязателен")
    @Min(value = 1, message = "Порядковый номер начинается с 1")
    private Integer orderIndex;

    // --- THEORY ---
    private String content;

    // --- Общее для задач ---
    private String description;

    // --- CHOICE_PROBLEM ---
    private List<String> options;
    private List<Integer> correctOptionIndexes;
    private Boolean isMultipleChoice;

    // --- INPUT_PROBLEM ---
    private String correctAnswer;

    // --- CODE_PROBLEM ---
    private Integer timeLimitSec;
    private Integer memoryLimitMb;
    private String allowedLanguages;
    private Integer ejudgeContestId;
    private String ejudgeProblemId;
}
