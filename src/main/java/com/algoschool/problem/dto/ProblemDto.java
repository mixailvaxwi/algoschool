package com.algoschool.problem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Задача банка для преподавателя.
 * <p>
 * Правильные ответы ({@code correctAnswer}, {@code correctOptionIndexes})
 * заполняются только автору задачи и только в карточке — в списке банка их нет
 * никогда. Студенческие DTO этот класс не используют вовсе.
 */
@Data
@Builder
public class ProblemDto {
    private Long id;
    private String problemType;
    private String title;
    private String description;
    private String difficulty;
    private String visibility;
    private Integer maxScore;
    private List<String> tags;

    private Long authorId;
    private String authorName;
    /** Правка и удаление доступны только автору; чужую публичную задачу можно лишь вставить в урок. */
    private Boolean editable;

    /** В скольких уроках задача сейчас стоит: правка условия затрагивает их все. */
    private Long usageCount;

    private Integer attemptedStudentsCount;
    private Integer successStudentsCount;
    private LocalDateTime updatedAt;

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
