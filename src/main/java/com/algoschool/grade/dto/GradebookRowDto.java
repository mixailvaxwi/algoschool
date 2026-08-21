package com.algoschool.grade.dto;

import java.util.Map;

/**
 * Строка журнала — один студент.
 *
 * @param scores оценки по идентификатору элемента; элементы без оценки в карте
 *               отсутствуют, а не лежат нулями: «не сдавал» и «сдал на ноль» —
 *               разные вещи
 */
public record GradebookRowDto(
        Long userId,
        String username,
        String name,
        Map<Long, GradeCellDto> scores,
        int totalScore,
        double percent
) {}
