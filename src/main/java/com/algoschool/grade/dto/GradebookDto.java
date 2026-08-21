package com.algoschool.grade.dto;

import java.util.List;

/** Журнал курса: строки — студенты, столбцы — оцениваемые элементы (UC-T-43). */
public record GradebookDto(
        Long courseId,
        String courseTitle,
        List<GradeItemDto> items,
        List<GradebookRowDto> rows,
        int totalMaxScore
) {}
