package com.mpanyavin.algoschool.module_course.dto;

public record CoursePurchaseResponse(
        boolean success,
        String message,
        Integer newBalance
) {}