package com.be_mxh.controller.client;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.client.auth.*;
import com.be_mxh.service.AuthService;
import com.be_mxh.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        // Check username duplicate
        if (userService.isDuplicateUsername(registerRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message("DUPLICATE")
                            .data("Username already exists")
                            .build());
        }

        // Check email duplicate
        if (userService.isDuplicateEmail(registerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message("DUPLICATE")
                            .data("Email already exists")
                            .build());
        }

        // Check confirm password
        if (!userService.isCorrectConfirmPassword(registerRequest, registerRequest.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message("INVALID_CONFIRM_PASSWORD")
                            .data("Confirm password is not correct")
                            .build());
        }

        // Success
        RegisterResponse result = userService.save(registerRequest);
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
