package com.algoschool.grade.dto;

/** Одна оценка студента. Ссылка на урок нужна, чтобы вернуться к задаче. */
public record MyGradeDto(
        Long itemId,
        String kind,
        Long stepId,
        Long lessonId,
        String title,
        Integer maxScore,
        Integer score,
        String comment
) {}
