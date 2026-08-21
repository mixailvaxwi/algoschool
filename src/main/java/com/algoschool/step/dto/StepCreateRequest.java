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
 * <p>
 * Для задач запрос работает в двух режимах: с {@link #problemId} — ставит в
 * урок готовую задачу из банка, без него — заводит новую задачу и сразу её
 * размещает (так работает форма редактора урока).
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
    /** Задача из банка. Если задан, остальные поля задачи игнорируются. */
    private Long problemId;
    /** Название задачи в банке. Если не задано, берётся первая строка условия. */
    private String title;
    private String description;
    private String difficulty;
    private String visibility;
    private Integer maxScore;
    private List<String> tags;

    // --- CHOICE_PROBLEM ---
    private List<String> options;
    private List<Integer> correctOptionIndexes;
    private Boolean isMultipleChoice;

    // --- INPUT_PROBLEM ---
    private String correctAnswer;

    // --- NUMERIC_PROBLEM ---
    private Double correctValue;
    private Double tolerance;
    private String toleranceKind;

    // --- MATCHING_PROBLEM ---
    private List<String> leftItems;
    private List<String> rightItems;

    // --- ORDERING_PROBLEM ---
    private List<String> orderedItems;

    // --- OPEN_ANSWER_PROBLEM ---
    private String reviewGuidelines;

    // --- CODE_PROBLEM ---
    private Integer timeLimitSec;
    private Integer memoryLimitMb;
    private String allowedLanguages;
    private Integer ejudgeContestId;
    private String ejudgeProblemId;
}
