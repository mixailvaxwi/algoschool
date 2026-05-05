package com.algoschool.module_user.service;

import com.algoschool.module_user.dto.JwtResponse;
import com.algoschool.module_user.dto.LoginRequest;
import com.algoschool.module_user.dto.RegisterRequest;

public interface AuthService {
    void registerUser(RegisterRequest request);
    JwtResponse authenticateUser(LoginRequest request);
}
