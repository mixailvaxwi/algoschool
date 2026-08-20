package com.algoschool.admin.dto;

import com.algoschool.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotNull(message = "Роль обязательна")
    private Role role;
}
