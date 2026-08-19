package com.algoschool.user.controller;

import com.algoschool.auth.dto.UserProfileDto;
import com.algoschool.user.dto.PublicProfileDto;
import com.algoschool.user.dto.UserProfileUpdateRequest;
import com.algoschool.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Получить СВОЙ профиль (токен берется из заголовка Authorization)
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(userService.getUserProfile(currentUser.getUsername()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateCurrentUser(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(currentUser.getUsername(), request));
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicProfileDto> getPublicProfile(@PathVariable String username) {
        return ResponseEntity.ok(userService.getPublicProfile(username));
    }
}