package com.mpanyavin.algoschool.module_user.dto;

public record JwtResponse(
        String token,
        String type, // Обычно "Bearer"
        Long id,
        String username,
        String email,
        String role,
        Integer balance // Фронтенду сразу полезно знать баланс при входе
) {
    public JwtResponse(String token, Long id, String username, String email, String role, Integer balance) {
        this(token, "Bearer", id, username, email, role, balance);
    }
}