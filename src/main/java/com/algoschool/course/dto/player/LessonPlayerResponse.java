package com.algoschool.course.dto.player;

import java.util.List;

/**
 * DTO для объединения контента урока и прогресса конкретного студента
 */
public record LessonPlayerResponse(
        LessonPlayerDto lesson,
        List<Long> completedSteps
) {}