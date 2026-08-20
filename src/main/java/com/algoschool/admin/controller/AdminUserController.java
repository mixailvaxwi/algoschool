package com.algoschool.admin.controller;

import com.algoschool.admin.dto.AdminUserDto;
import com.algoschool.admin.dto.PasswordResetResponse;
import com.algoschool.admin.dto.RoleUpdateRequest;
import com.algoschool.admin.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Доступ ограничен ролью ROLE_ADMIN на уровне SecurityConfig (/api/admin/**).
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserDto>> searchUsers(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(adminUserService.searchUsers(q));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDto> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getUser(userId));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<AdminUserDto> updateRole(
            @PathVariable Long userId,
            @Valid @RequestBody RoleUpdateRequest request,
            @AuthenticationPrincipal UserDetails currentAdmin) {
        return ResponseEntity.ok(adminUserService.updateRole(userId, request.getRole(), currentAdmin.getUsername()));
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<AdminUserDto> blockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentAdmin) {
        return ResponseEntity.ok(adminUserService.setLocked(userId, true, currentAdmin.getUsername()));
    }

    @PostMapping("/{userId}/unblock")
    public ResponseEntity<AdminUserDto> unblockUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails currentAdmin) {
        return ResponseEntity.ok(adminUserService.setLocked(userId, false, currentAdmin.getUsername()));
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.resetPassword(userId));
    }
}
