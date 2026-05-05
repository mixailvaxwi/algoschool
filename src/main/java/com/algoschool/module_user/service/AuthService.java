package com.mpanyavin.algoschool.module_user.service;

import com.mpanyavin.algoschool.module_user.dto.JwtResponse;
import com.mpanyavin.algoschool.module_user.dto.LoginRequest;
import com.mpanyavin.algoschool.module_user.dto.RegisterRequest;

public interface AuthService {
    void registerUser(RegisterRequest request);
    JwtResponse authenticateUser(LoginRequest request);
}
