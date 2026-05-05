package com.algoschool.module_assessment.dto;

public record SubmissionRequest(
        // Сюда придет текст ответа, индекс радио-кнопки (в виде строки "1") или весь исходный код
        String answer
) {}