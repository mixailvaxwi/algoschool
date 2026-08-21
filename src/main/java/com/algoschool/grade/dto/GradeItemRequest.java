package com.algoschool.grade.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Создание ручного элемента журнала и правка любого элемента. */
@Data
public class GradeItemRequest {

    @NotBlank(message = "Название элемента обязательно")
    @Size(max = 200, message = "Название не длиннее 200 символов")
    private String title;

    @NotNull(message = "Вес элемента обязателен")
    @Min(value = 1, message = "Вес элемента — минимум 1 балл")
    private Integer maxScore;

    /** BEST / LAST / FIRST / AVERAGE. У ручного элемента не используется. */
    private String policy;
}
