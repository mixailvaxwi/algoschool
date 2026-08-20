package com.algoschool.step.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Полное представление шага для преподавателя — в отличие от плеерных DTO
 * содержит правильные ответы, чтобы их можно было показать в форме редактирования.
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
