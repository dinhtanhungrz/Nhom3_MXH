package com.be_mxh.service;

import com.be_mxh.dto.auth.*;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    boolean isDuplicateUsername(String username);

    boolean isDuplicateEmail(String email);

    RegisterResponse register(RegisterRequest registerRequest);

    RefreshTokenResponse refreshToken(String refreshToken);
}
