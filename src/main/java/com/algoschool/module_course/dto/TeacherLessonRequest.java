package com.algoschool.module_course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminLessonRequest(
        @NotBlank(message = "Название урока не может быть пустым")
        String title,

        @NotNull(message = "Порядковый номер обязателен")
        Integer orderIndex
) {}