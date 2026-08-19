package com.algoschool.course.dto.student;

import com.algoschool.course.entity.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrollmentResponse {
    private Long courseId;
    private ApplicationStatus status;
    private String message; // Например: "Вы успешно зачислены!" или "Заявка отправлена на рассмотрение"
}