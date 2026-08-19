package com.algoschool.course.dto.student;

import lombok.Data;

@Data
public class EnrollmentRequest {
    // Опциональное поле. Если курс открытый, фронтенд может прислать пустой объект {}
    private String motivationMessage;
}