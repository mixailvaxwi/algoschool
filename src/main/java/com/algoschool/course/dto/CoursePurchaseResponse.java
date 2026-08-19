package com.algoschool.course.dto;

public record CoursePurchaseResponse(
        boolean success,
        String message,
        Integer newBalance
) {}