package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.auth.*;
import com.be_mxh.service.AuthService;
import com.be_mxh.validation.PasswordValidator;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check username duplicate
        if (authService.isDuplicateUsername(registerRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message("Username already exists")
                            .data("DUPLICATE")
                            .build());
        }

        // Check email duplicate
        if (authService.isDuplicateEmail(registerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message("Email already exists")
                            .data("DUPLICATE")
                            .build());
        }

        // Check confirm password
        if (!PasswordValidator.isConfirmPasswordMatched(registerRequest.getPassword(), registerRequest.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message("Confirm password is not correct")
                            .data("INVALID_CONFIRM_PASSWORD")
                            .build());
        }

        // Success
        RegisterResponse result = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<RegisterResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("User registered successfully")
                        .data(result)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<LoginResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Login successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<RefreshTokenResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Refresh token successfully!")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Logout successfully!")
                        .data(null)
                        .build()
        );
    }


}
