package com.algoschool.module_course.dto;

import com.algoschool.module_course.entity.AccessType;

public record CourseCatalogResponse(
        Long id,
        String title,
        String description,
        AccessType accessType, // ИЗМЕНЕНО: вместо int price
        boolean isEnrolled     // ИЗМЕНЕНО: вместо isPurchased/isOwned
) {}