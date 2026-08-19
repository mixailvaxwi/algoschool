package com.algoschool.course.dto.player;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TheoryStepPlayerDto extends StepPlayerDto {
    private String content;
}