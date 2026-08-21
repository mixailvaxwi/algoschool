package com.algoschool.course.dto.player;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Развёрнутый ответ. Критерии проверки — это подсказка проверяющему, а не
 * студенту, поэтому сюда они не попадают.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OpenAnswerProblemPlayerDto extends StepPlayerDto {
    private String description;
}
