package com.emotion.user.service;

import com.emotion.user.dto.*;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    LoginResponse refresh(RefreshTokenRequest request);

    void logout(HttpServletRequest request);
}
