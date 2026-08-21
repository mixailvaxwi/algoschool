package com.algoschool.grade.dto;

import java.util.List;

/** Свои оценки по курсу в режиме чтения (UC-S-33). */
public record MyGradesDto(
        Long courseId,
        String courseTitle,
        List<MyGradeDto> grades,
        int totalScore,
        int totalMaxScore,
        double percent
) {}
