package com.mpanyavin.algoschool.module_user.service;

import com.mpanyavin.algoschool.module_user.dto.UserProfileResponse;
import com.mpanyavin.algoschool.module_user.entity.User;
import com.mpanyavin.algoschool.module_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // Оптимизация: транзакция только для чтения
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден")); // Позже заменим на кастомный Exception

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getTotalXp(),
                user.getBalance()
        );
    }
}