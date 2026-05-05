package com.algoschool.module_course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminCourseRequest(
        @NotBlank(message = "Название курса не может быть пустым")
        String title,

        @NotBlank(message = "Описание не может быть пустым")
        String description,

        @NotNull(message = "Цена должна быть указана")
        @Min(value = 0, message = "Цена не может быть отрицательной")
        Integer price,

        @NotNull(message = "Статус публикации должен быть указан")
        Boolean isPublished
) {}