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

    // --- NUMERIC_PROBLEM ---
    private Double correctValue;
    private Double tolerance;
    /** ABSOLUTE (по умолчанию) или RELATIVE. */
    private String toleranceKind;

    // --- MATCHING_PROBLEM ---
    // Параллельные списки: leftItems[i] соответствует rightItems[i]. Автор
    // вводит пары, перемешиванием занимается сервер.
    private List<String> leftItems;
    private List<String> rightItems;

    // --- ORDERING_PROBLEM ---
    /** Элементы в правильном порядке — в том, в каком их вводит автор. */
    private List<String> orderedItems;

    // --- OPEN_ANSWER_PROBLEM ---
    /** Критерии для проверяющего; студенту не показываются. */
    private String reviewGuidelines;

    // --- CODE_PROBLEM ---
    private Integer timeLimitSec;
    private Integer memoryLimitMb;
    private String allowedLanguages;
    private Integer ejudgeContestId;
    private String ejudgeProblemId;
}
