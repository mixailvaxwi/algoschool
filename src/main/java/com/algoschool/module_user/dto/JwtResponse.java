package com.algoschool.module_user.dto;

public record JwtResponse(
        String token,
        String type, // Обычно "Bearer"
        Long id,
        String username,
        String email,
        String role
) {
    public JwtResponse(String token, Long id, String username, String email, String role) {
        this(token, "Bearer", id, username, email, role);
    }
}