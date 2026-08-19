package com.algoschool.submission.dto.teacher;

import com.algoschool.submission.entity.SubmissionStatus;
import java.time.LocalDateTime;

public record TeacherSubmissionDto(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseTitle,
        Integer moduleIndex,
        Integer lessonIndex,
        Integer stepIndex,
        String payload,
        SubmissionStatus status,
        LocalDateTime createdAt
) {}