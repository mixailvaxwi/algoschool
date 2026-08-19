package com.algoschool.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
        @NotBlank(message = "Имя пользователя обязательно")
        @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
        private String username;

        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        private String email;

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
        private String password;

        private String name;

        // Роль сознательно НЕ принимается от клиента: иначе любой желающий
        // регистрируется преподавателем или админом. Все новые пользователи —
        // студенты; повышение роли делается отдельной админской операцией.
}
