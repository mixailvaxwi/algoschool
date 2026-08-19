package com.algoschool.course.dto.teacher;

import com.algoschool.course.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationStatusUpdateRequest {
    @NotNull(message = "Статус не может быть пустым")
    private ApplicationStatus status; // Ожидаем APPROVED или REJECTED
}