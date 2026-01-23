package com.be_mxh.service;

import com.be_mxh.dto.client.auth.LoginRequest;
import com.be_mxh.dto.client.auth.LoginResponse;

public interface AuthService {
    String login(LoginRequest loginRequest);
}
