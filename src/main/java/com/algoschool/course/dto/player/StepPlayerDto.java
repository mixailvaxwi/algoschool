package com.algoschool.course.dto.player;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
// Эта аннотация заставит Spring добавить поле "stepType" в JSON ответ
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "stepType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TheoryStepPlayerDto.class, name = "THEORY"),
        @JsonSubTypes.Type(value = CodeProblemPlayerDto.class, name = "CODE_PROBLEM"),
        @JsonSubTypes.Type(value = TextProblemPlayerDto.class, name = "INPUT_PROBLEM"),
        @JsonSubTypes.Type(value = ChoiceProblemPlayerDto.class, name = "CHOICE_PROBLEM"),
        @JsonSubTypes.Type(value = NumericProblemPlayerDto.class, name = "NUMERIC_PROBLEM"),
        @JsonSubTypes.Type(value = MatchingProblemPlayerDto.class, name = "MATCHING_PROBLEM"),
        @JsonSubTypes.Type(value = OrderingProblemPlayerDto.class, name = "ORDERING_PROBLEM"),
        @JsonSubTypes.Type(value = OpenAnswerProblemPlayerDto.class, name = "OPEN_ANSWER_PROBLEM")
})
public abstract class StepPlayerDto {
    private Long id;
    private Integer orderIndex;
}