package com.algoschool.submission.dto.teacher;

import java.time.LocalDateTime;

/**
 * Развёрнутый ответ, ожидающий проверки (UC-T-40).
 *
 * @param reviewGuidelines критерии, которые автор задачи оставил проверяющему;
 *                         студенту они не отдаются никогда
 */
public record ReviewItemDto(
        Long submissionId,
        Long studentId,
        String studentName,
        Long courseId,
        String courseTitle,
        Long lessonId,
        String lessonTitle,
        Long stepId,
        String problemTitle,
        String description,
        String reviewGuidelines,
        String answer,
        Integer maxScore,
        LocalDateTime submittedAt
) {}
