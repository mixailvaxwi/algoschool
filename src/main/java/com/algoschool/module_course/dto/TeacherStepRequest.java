package com.algoschool.module_course.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "stepType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TeacherTheoryStepRequest.class, name = "THEORY"),
        @JsonSubTypes.Type(value = TeacherCodeProblemRequest.class, name = "CODE_PROBLEM"),
        @JsonSubTypes.Type(value = TeacherInputProblemRequest.class, name = "INPUT_PROBLEM"),
        @JsonSubTypes.Type(value = TeacherChoiceProblemRequest.class, name = "CHOICE_PROBLEM")
})
public abstract class TeacherStepRequest {
    private Integer orderIndex;
}