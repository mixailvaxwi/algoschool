package com.algoschool.auth.service;

import com.algoschool.auth.dto.AuthResponse;
import com.algoschool.auth.dto.LoginRequest;
import com.algoschool.auth.dto.RegisterRequest;
import com.algoschool.user.dto.UserProfileDto;
import com.algoschool.exception.AppException;
import com.algoschool.user.entity.Role;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService; // Ваш сервис генерации токенов

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Проверка уникальности
        if (userRepository.existsByUsername(request.getUsername())) {
            throw AppException.conflict("Пользователь с таким логином уже существует");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.conflict("Пользователь с таким email уже существует");
        }

        // Создаем пользователя
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                // Роль жёстко фиксируется здесь и никогда не приходит от клиента:
                // иначе регистрация становится способом выдать себе ROLE_ADMIN.
                .role(Role.ROLE_STUDENT)
                .build();

        user = userRepository.save(user);

        // Генерируем токен
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserProfile(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Spring Security сам проверит пароль. Если не совпадает - выбросит исключение (BadCredentialsException)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserProfile(user))
                .build();
    }

    private UserProfileDto mapToUserProfile(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}