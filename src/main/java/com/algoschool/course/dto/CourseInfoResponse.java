package com.algoschool.course.dto;

public record CourseInfoResponse(
        Long id,
        String title,
        String description,
        String accessType,
        boolean isEnrolled,
        String applicationStatus
) {}