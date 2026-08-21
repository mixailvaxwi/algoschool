package com.algoschool.course.dto.player;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Соответствие. Правая колонка приходит перемешанной — иначе i-й правый
 * элемент подходил бы к i-му левому, и задача решалась бы чтением ответа API.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MatchingProblemPlayerDto extends StepPlayerDto {
    private String description;
    private List<String> leftItems;
    private List<String> rightItems;
}
