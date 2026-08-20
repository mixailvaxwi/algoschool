package com.algoschool.admin.dto;

import com.algoschool.user.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserDto {
    private Long id;
    private String username;
    private String email;
    private String name;
    private Role role;
    private boolean locked;
}
