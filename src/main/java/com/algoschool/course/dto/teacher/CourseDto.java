package com.algoschool.course.dto.teacher;

import com.algoschool.course.entity.AccessType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private AccessType accessType;

    // Жестко фиксируем имя ключа в JSON
    @JsonProperty("isPublished")
    private boolean isPublished;
}