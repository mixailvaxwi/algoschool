package com.algoschool.course.dto;

import com.algoschool.course.entity.AccessType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private AccessType accessType;
    private boolean isPublished;
    // Можно добавить имя автора, если нужно отображать его на карточке курса:
    private String authorName;
}