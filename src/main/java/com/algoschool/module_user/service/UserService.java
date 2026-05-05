package com.algoschool.module_user.service;

import com.algoschool.module_user.dto.UserProfileResponse;

public interface UserService {
    UserProfileResponse getUserProfile(Long userId);
}