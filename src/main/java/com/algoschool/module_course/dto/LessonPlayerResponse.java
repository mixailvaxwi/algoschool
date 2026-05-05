package com.algoschool.module_course.dto;

import com.algoschool.module_course.entity.Lesson;

import java.util.Set;

public record LessonPlayerResponse(
        Lesson lesson,
        Set<Long> completedSteps
) {}