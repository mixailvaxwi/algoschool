package com.algoschool.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- МЕТОДЫ ИНТЕРФЕЙСА UserDetails (Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Преобразуем наш Enum Role (например, ROLE_STUDENT) в формат GrantedAuthority
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        // Spring Security использует этот метод для получения хэша пароля из БД
        return passwordHash;
    }

    @Override
    public String getUsername() {
        // Возвращает логин пользователя для формирования токена и поиска в БД
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Можно реализовать логику устаревания аккаунта, но по умолчанию возвращаем true
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Можно реализовать логику бана пользователя
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Логика устаревания пароля
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Подтверждение Email и активация аккаунта
        return true;
    }
}