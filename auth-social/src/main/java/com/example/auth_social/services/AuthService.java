package com.example.auth_social.services;


import com.example.auth_social.dto.*;

public interface AuthService {
    UserResponse signup(SignUpRequest request);
    LoginResponse login(LoginRequest request);
}
