package com.algoschool.module_user.service;

import com.algoschool.module_user.dto.JwtResponse;
import com.algoschool.module_user.dto.LoginRequest;
import com.algoschool.module_user.dto.RegisterRequest;
import com.algoschool.module_user.entity.Role;
import com.algoschool.module_user.entity.User;
import com.algoschool.module_user.repository.UserRepository;
import com.algoschool.security.JwtTokenProvider;
import com.algoschool.security.UserDetailsImpl;
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
            throw new RuntimeException("Этот логин уже занят");
        }

        User user = new User();
        user.setName(request.name());
        user.setUsername(request.username());
        user.setEmail(request.email());

        // ЛОГИКА ВЫБОРА РОЛИ
        if ("TEACHER".equalsIgnoreCase(request.role())) {
            user.setRole(Role.ROLE_TEACHER);
        } else {
            user.setRole(Role.ROLE_STUDENT);
        }
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        // 3. ЛОГИКА ВЫБОРА РОЛИ
        // Проверяем поле role, которое пришло из JSON с фронтенда
        if ("TEACHER".equalsIgnoreCase(request.role())) {
            user.setRole(Role.ROLE_TEACHER);
        } else {
            // По умолчанию всегда студент, даже если с фронта пришел мусор
            user.setRole(Role.ROLE_STUDENT);
        }

        // 4. Сохраняем в базу через репозиторий
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
                user.getRole().name()
        );
    }
}