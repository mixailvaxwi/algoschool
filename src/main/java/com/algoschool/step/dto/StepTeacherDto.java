package com.algoschool.step.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Полное представление шага для преподавателя — в отличие от плеерных DTO
 * содержит правильные ответы, чтобы их можно было показать в форме редактирования.
 * <p>
 * Содержание задачи приезжает вместе с шагом, как и раньше, но живёт оно
 * теперь в банке: {@link #problemUsageCount} говорит, сколько уроков затронет
 * правка, а {@link #problemEditable} — можно ли править её отсюда вообще
 * (чужую публичную задачу разрешено только поставить в урок).
 */
@Data
@Builder
public class StepTeacherDto {
    private Long id;
    private String stepType;
    private Integer orderIndex;

    // --- THEORY ---
    private String content;

    // --- Общее для задач ---
    private Long problemId;
    private String title;
    private Long problemUsageCount;
    private Boolean problemEditable;
    private String visibility;
    private String difficulty;
    private Integer maxScore;
    private List<String> tags;
    private String description;

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
