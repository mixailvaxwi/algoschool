package com.algoschool.course.dto;

import com.algoschool.course.entity.AccessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TeacherCourseRequest(
        @NotBlank(message = "Название курса не может быть пустым")
        @Size(max = 255, message = "Название курса не длиннее 255 символов")
        String title,

        String description,

        @NotNull(message = "Тип доступа обязателен: OPEN или CLOSED")
        AccessType accessType
) {}
