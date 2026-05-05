package com.mpanyavin.algoschool.module_user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // Явно указываем имя таблицы во множественном числе
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING) // Храним в БД как строку (ROLE_STUDENT), а не как цифру (0)
    @Column(nullable = false)
    private Role role;

    // Геймификация: суммарный опыт
    @Column(name = "total_xp", nullable = false)
    @Builder.Default
    private Integer totalXp = 0;

    // Монетизация: баланс внутренней валюты
    @Column(nullable = false)
    @Builder.Default
    private Integer balance = 0;
}