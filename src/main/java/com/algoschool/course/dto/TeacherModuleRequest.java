package com.algoschool.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeacherModuleRequest(
        @NotBlank(message = "Название модуля не может быть пустым")
        String title,

        @NotNull(message = "Порядковый номер обязателен")
        Integer orderIndex
) {}