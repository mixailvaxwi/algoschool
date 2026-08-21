package com.algoschool.course.dto.player;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Упорядочивание. Элементы приходят уже перемешанными — сервер применил
 * порядок показа. Правильную последовательность из этого ответа не вывести.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderingProblemPlayerDto extends StepPlayerDto {
    private String description;
    private List<String> items;
}
