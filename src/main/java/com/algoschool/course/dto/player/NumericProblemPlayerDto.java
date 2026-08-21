package com.algoschool.course.dto.player;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Числовой ответ. Допуск студенту показывается намеренно: не зная требуемой
 * точности, он не понимает, сколько знаков округлять. Эталонное значение,
 * разумеется, не отдаётся.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NumericProblemPlayerDto extends StepPlayerDto {
    private String description;
    private Double tolerance;
    private String toleranceKind;
}
