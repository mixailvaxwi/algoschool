package com.algoschool.course.dto;

import com.algoschool.course.entity.AccessType;

public record TeacherCourseRequest(
        String title,
        String description,
        AccessType accessType
) {}