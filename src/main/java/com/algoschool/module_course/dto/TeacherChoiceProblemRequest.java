package com.algoschool.module_course.dto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherChoiceProblemRequest extends TeacherBaseProblemRequest {
    private List<String> options;
    private Integer correctOptionIndex;
}