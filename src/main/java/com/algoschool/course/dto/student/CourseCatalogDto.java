package com.algoschool.course.dto.student;

import com.algoschool.course.entity.AccessType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseCatalogDto {
    private Long id;
    private String title;
    private String description;
    private AccessType accessType;
    private String authorName;
    private Integer lessonsCount;

    @JsonProperty("isEnrolled")
    private boolean isEnrolled;

    @JsonProperty("applicationStatus")
    private String applicationStatus;
}