package com.algoschool.module_course.dto;

public record CourseInfoResponse(
        Long id,
        String title,
        String description,
        String accessType,
        boolean isEnrolled,
        String applicationStatus
) {}