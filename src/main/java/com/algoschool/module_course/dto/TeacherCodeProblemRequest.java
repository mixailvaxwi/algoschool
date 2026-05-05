package com.algoschool.module_course.dto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherCodeProblemRequest extends TeacherBaseProblemRequest {
    private Integer timeLimitSec;
    private Integer memoryLimitMb;
    private String allowedLanguages;
}