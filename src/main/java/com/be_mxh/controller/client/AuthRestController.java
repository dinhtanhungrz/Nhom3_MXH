package com.be_mxh.controller.client;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.client.auth.RegisterRequest;
import com.be_mxh.dto.client.auth.RegisterResponse;
import com.be_mxh.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest registerRequest, BindingResult bindingResult) {
        // Validate field
        if (bindingResult.hasFieldErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Check username duplicate
        if (userService.isDuplicateUsername(registerRequest.getUsername())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Check confirm password
        if (!userService.isCorrectConfirmPassword(registerRequest, registerRequest.getConfirmPassword())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        RegisterResponse result = userService.save(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<RegisterResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("User registered successfully")
                        .data(result)
                        .build()
        );
    }
}
