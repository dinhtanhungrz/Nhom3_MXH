package com.be_mxh.service;

import com.be_mxh.dto.auth.LoginRequest;
import com.be_mxh.dto.auth.LoginResponse;
import com.be_mxh.dto.auth.RegisterRequest;
import com.be_mxh.dto.auth.RegisterResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    boolean isDuplicateUsername(String username);

    boolean isDuplicateEmail(String email);

    RegisterResponse register(RegisterRequest registerRequest);

}
