package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.user.UpdatePasswordRequest;
import com.be_mxh.dto.user.UpdateProfileRequest;
import com.be_mxh.dto.user.UserProfileResponse;
import com.be_mxh.dto.user.UserResponse;
import com.be_mxh.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/users")
public class UserRestController {
    @Autowired
    private UserService userService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/profile")
    public ResponseEntity<?> getUserInfo() {
        UserProfileResponse user = userService.getProfile();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<UserProfileResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get current user successfully")
                        .data(user)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(@Valid @ModelAttribute UpdateProfileRequest updateProfileRequest) {
        UserProfileResponse user = userService.updateProfile(updateProfileRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UserProfileResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Update user successfully")
                        .data(user)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        // Check confirm password
        if (!userService.isCorrectConfirmPassword(updatePasswordRequest.getPassword(), updatePasswordRequest.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message("INVALID_CONFIRM_PASSWORD")
                            .data("Confirm password is not correct")
                            .build());
        }

        userService.updatePassword(updatePasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Update password successfully")
                        .data(null)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<List<UserResponse>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get all users successfully!")
                        .data(users)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/block/{id}")
    public ResponseEntity<?> blockUser(@PathVariable("id") Long id) {
        UserResponse user = userService.blockUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<UserResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Lock user '" + user.getUsername() + "' successfully!")
                        .data(user)
                        .build()
        );
    }

}