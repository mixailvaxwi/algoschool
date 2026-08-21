package com.algoschool.grade.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Ручная оценка студента за элемент журнала (UC-T-41). */
@Data
public class GradeUpdateRequest {

    @NotNull(message = "Элемент журнала обязателен")
    private Long gradeItemId;

    @NotNull(message = "Студент обязателен")
    private Long userId;

    @NotNull(message = "Балл обязателен")
    @Min(value = 0, message = "Балл не может быть отрицательным")
    private Integer score;

    /** Обязателен: ручная правка балла должна быть объяснена. */
    private String comment;
}
