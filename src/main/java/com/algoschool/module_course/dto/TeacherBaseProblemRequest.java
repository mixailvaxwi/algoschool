package com.algoschool.module_course.dto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TeacherBaseProblemRequest extends TeacherStepRequest {
    private String description;
    private Integer difficulty;
    private Integer xpReward;
}