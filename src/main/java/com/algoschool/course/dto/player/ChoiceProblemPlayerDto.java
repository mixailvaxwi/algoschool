package com.algoschool.course.dto.player;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceProblemPlayerDto extends StepPlayerDto {
    private String description;
    private List<String> options;
    private boolean isMultipleChoice;
    // ВАЖНО: Никакого correctOptionIndex!
}