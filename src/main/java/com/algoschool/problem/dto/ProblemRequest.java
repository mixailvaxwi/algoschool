package com.algoschool.problem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Создание и правка задачи в банке.
 * <p>
 * Обязательными помечены только поля, общие для всех типов; остальное
 * проверяет {@code ProblemContentMapper} после разбора problemType — так же,
 * как это устроено у шагов урока.
 */
@Data
public class ProblemRequest {

    @NotBlank(message = "Тип задачи обязателен")
    private String problemType;

    @NotBlank(message = "Название обязательно")
    @Size(max = 200, message = "Название не длиннее 200 символов")
    private String title;

    @NotBlank(message = "Условие задачи обязательно")
    private String description;

    /** EASY / MEDIUM / HARD либо пусто, если автор не хочет указывать. */
    private String difficulty;

    /** PRIVATE (по умолчанию) или PUBLIC. */
    private String visibility;

    @Min(value = 1, message = "Вес задачи — минимум 1 балл")
    private Integer maxScore;

    private List<String> tags;

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
