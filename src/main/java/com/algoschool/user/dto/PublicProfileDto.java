package com.algoschool.user.dto;

import com.algoschool.user.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicProfileDto {
    private Long id;
    private String username;
    private String name;
    private Role role;
}