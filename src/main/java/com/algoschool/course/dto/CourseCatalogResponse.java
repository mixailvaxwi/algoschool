package com.algoschool.course.dto;

import com.algoschool.course.entity.AccessType;

public record CourseCatalogResponse(
        Long id,
        String title,
        String description,
        AccessType accessType, // ИЗМЕНЕНО: вместо int price
        boolean isEnrolled     // ИЗМЕНЕНО: вместо isPurchased/isOwned
) {}