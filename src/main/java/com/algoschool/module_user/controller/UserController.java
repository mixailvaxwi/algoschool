package com.mpanyavin.algoschool.module_user.controller;

import com.mpanyavin.algoschool.module_user.dto.UserProfileResponse;
import com.mpanyavin.algoschool.module_user.service.UserService;
import com.mpanyavin.algoschool.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Эндпоинт доступен только авторизованным пользователям (настроено в SecurityConfig)
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // Достаем ID из токена и идем с ним в базу за свежими данными
        UserProfileResponse profile = userService.getUserProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }
}