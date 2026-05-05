package com.algoschool.module_course.dto;

import java.util.List;

// Эти записи в точности повторяют интерфейсы Lesson и Module из нашего React-кода!
public record CourseStructureResponse(
        Long id,
        String title,
        Integer orderIndex,
        List<LessonDto> lessons
) {
    public record LessonDto(
            Long id,
            String title,
            Integer orderIndex
    ) {}
}