package com.algoschool.course.dto.player;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LessonPlayerDto {
    private Long id;
    private String title;
    private Integer orderIndex;
    private List<StepPlayerDto> steps;
}