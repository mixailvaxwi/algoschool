package com.mpanyavin.algoschool.module_assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmissionRequest(
        @NotNull(message = "ID задачи не может быть пустым")
        Long problemId,

        @NotBlank(message = "Ответ не может быть пустым")
        String payload // Введенный текст, ID выбранных вариантов (через запятую) или исходный код
) {}