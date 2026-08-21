package com.algoschool.problem.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Числовой ответ с допуском.
 * <p>
 * Отдельный тип, а не текстовый ответ со сравнением строк: «0.33», «0,33» и
 * «0.3300» — одно и то же число, а точный ввод считал бы их разными ответами.
 */
@Entity
@Table(name = "numeric_problems")
@Getter @Setter
public class NumericProblem extends Problem {

    @Column(name = "correct_value", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Double correctValue;

    @Column(nullable = false)
    private Double tolerance = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "tolerance_kind", nullable = false, length = 16)
    private ToleranceKind toleranceKind = ToleranceKind.ABSOLUTE;
}
