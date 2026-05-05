package com.algoschool.module_course.dto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherInputProblemRequest extends TeacherBaseProblemRequest {
    private String correctAnswer;
}