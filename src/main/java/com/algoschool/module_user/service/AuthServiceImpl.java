package com.mpanyavin.algoschool.module_user.service;

import com.mpanyavin.algoschool.module_user.dto.JwtResponse;
import com.mpanyavin.algoschool.module_user.dto.LoginRequest;
import com.mpanyavin.algoschool.module_user.dto.RegisterRequest;
import com.mpanyavin.algoschool.module_user.entity.Role;
import com.mpanyavin.algoschool.module_user.entity.User;
import com.mpanyavin.algoschool.module_user.repository.UserRepository;
import com.mpanyavin.algoschool.security.JwtTokenProvider;
import com.mpanyavin.algoschool.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        // 1. Проверяем, не занят ли username или email
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Ошибка: Имя пользователя уже занято!"); // Позже заменим на кастомный Exception
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Ошибка: Email уже используется!");
        }

        // 2. Создаем нового пользователя
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password())) // Хешируем пароль!
                .role(Role.ROLE_STUDENT) // По умолчанию все новые пользователи — студенты
                .balance(0) // Стартовый баланс внутренней валюты
                .totalXp(0) // Стартовый опыт
                .build();

        // 3. Сохраняем в БД
        userRepository.save(user);
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest request) {
        // 1. Spring Security сам проверит совпадение хешей паролей
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. Устанавливаем пользователя в контекст безопасности
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Генерируем JWT токен
        String jwt = jwtTokenProvider.generateToken(authentication);

        // 4. Достаем данные пользователя для формирования ответа клиенту
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Получаем свежие данные о балансе из БД (опционально, но надежно)
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return new JwtResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getBalance()
        );
    }
}