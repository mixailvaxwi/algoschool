package com.algoschool.submission.dto.teacher;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Результат ручной проверки развёрнутого ответа. */
@Data
public class ReviewRequest {

    @NotNull(message = "Балл обязателен")
    @Min(value = 0, message = "Балл не может быть отрицательным")
    private Integer score;

    /** Обязателен: студент должен понимать, за что получил этот балл. */
    private String comment;
}
