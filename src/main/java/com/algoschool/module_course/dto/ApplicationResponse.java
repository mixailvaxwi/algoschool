package com.algoschool.module_course.dto;

import com.algoschool.module_course.entity.ApplicationStatus;
import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long courseId,
        String courseTitle,
        Long studentId,
        String studentName,
        String motivationMessage,
        ApplicationStatus status,
        LocalDateTime createdAt
) {}