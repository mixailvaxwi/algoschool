package com.algoschool.course.dto.player;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextProblemPlayerDto extends StepPlayerDto {
    private String description;
    // ВАЖНО: Никакого correctAnswer здесь нет! Студент его не получит.
}