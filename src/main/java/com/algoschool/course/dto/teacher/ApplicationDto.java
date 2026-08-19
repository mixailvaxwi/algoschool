package com.algoschool.course.dto.teacher;

import com.algoschool.course.entity.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail; // Почта нужна учителю для связи
    private String motivationMessage;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
}