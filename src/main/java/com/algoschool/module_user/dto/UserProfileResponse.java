package com.algoschool.module_user.dto;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String role
) {}