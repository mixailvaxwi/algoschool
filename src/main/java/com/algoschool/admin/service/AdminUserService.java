package com.algoschool.admin.service;

import com.algoschool.admin.dto.AdminUserDto;
import com.algoschool.admin.dto.PasswordResetResponse;
import com.algoschool.exception.AppException;
import com.algoschool.user.entity.Role;
import com.algoschool.user.entity.User;
import com.algoschool.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.List;

/**
 * Управление пользователями администратором (UC-A-01…04).
 * <p>
 * Заменяет ручной {@code UPDATE users SET role=...}, описанный в README как
 * единственный сегодня способ выдать первую роль преподавателя.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final String PASSWORD_CHARS =
            "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int GENERATED_PASSWORD_LENGTH = 14;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    @Transactional(readOnly = true)
    public List<AdminUserDto> searchUsers(String query) {
        String q = StringUtils.hasText(query) ? query.trim() : "";
        List<User> users = q.isEmpty()
                ? userRepository.findAll()
                : userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCase(q, q, q);

        return users.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AdminUserDto getUser(Long userId) {
        return toDto(findUser(userId));
    }

    @Transactional
    public AdminUserDto updateRole(Long userId, Role newRole, String actingAdminUsername) {
        User user = findUser(userId);

        // Иначе администратор, случайно сняв с себя роль, теряет доступ
        // к панели без запасного пути, кроме прямой правки базы.
        if (user.getUsername().equals(actingAdminUsername) && newRole != Role.ROLE_ADMIN) {
            throw AppException.badRequest("Нельзя снять с себя роль администратора");
        }

        user.setRole(newRole);
        return toDto(userRepository.save(user));
    }

    @Transactional
    public AdminUserDto setLocked(Long userId, boolean locked, String actingAdminUsername) {
        User user = findUser(userId);

        if (locked && user.getUsername().equals(actingAdminUsername)) {
            throw AppException.badRequest("Нельзя заблокировать самого себя");
        }

        user.setLocked(locked);
        return toDto(userRepository.save(user));
    }

    @Transactional
    public PasswordResetResponse resetPassword(Long userId) {
        User user = findUser(userId);

        String newPassword = generatePassword();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return PasswordResetResponse.builder().newPassword(newPassword).build();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("Пользователь не найден"));
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(GENERATED_PASSWORD_LENGTH);
        for (int i = 0; i < GENERATED_PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    private AdminUserDto toDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .locked(user.isLocked())
                .build();
    }
}
