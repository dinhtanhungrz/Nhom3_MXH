package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.auth.LoginRequest;
import com.be_mxh.dto.auth.LoginResponse;
import com.be_mxh.dto.auth.RegisterRequest;
import com.be_mxh.dto.auth.RegisterResponse;
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
        String accessToken = authService.login(loginRequest);
        LoginResponse result = new LoginResponse("Bearer", accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<LoginResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Login successfully")
                        .data(result)
                        .build()
        );
    }


}
