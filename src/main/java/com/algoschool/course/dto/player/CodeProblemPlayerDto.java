package com.algoschool.course.dto.player;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CodeProblemPlayerDto extends StepPlayerDto {
    private String description;
    private Integer timeLimitSec;
    private Integer memoryLimitMb;
    private String allowedLanguages;
}