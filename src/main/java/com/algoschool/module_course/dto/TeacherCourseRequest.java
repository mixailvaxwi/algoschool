package com.algoschool.module_course.dto;

import com.algoschool.module_course.entity.AccessType;

public record TeacherCourseRequest(
        String title,
        String description,
        AccessType accessType,
        boolean isPublished
) {}