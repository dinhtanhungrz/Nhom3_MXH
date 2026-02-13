package com.be_mxh.controller;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.user.*;
import com.be_mxh.service.FriendshipService;
import com.be_mxh.service.UserService;
import com.be_mxh.validation.PasswordValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    @Autowired
    private UserService userService;
    @Autowired
    private FriendshipService friendshipService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfoTerm() {
        ProfileResponse user = userService.getProfile();
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<ProfileResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get current user successfully")
                        .data(user)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        UserProfileResponse response = userService.getUserProfile(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<UserProfileResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Get user profile successfully")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(@Valid @ModelAttribute ProfileRequest profileRequest) {
        ProfileResponse user = userService.updateProfile(profileRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProfileResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Update user successfully")
                        .data(user)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        // Check confirm password
        if (!PasswordValidator.isConfirmPasswordMatched(updatePasswordRequest.getPassword(), updatePasswordRequest.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<String>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .message("Confirm password is not correct")
                            .data("INVALID_CONFIRM_PASSWORD")
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
    @PatchMapping("/block/{id}") // PATCH
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

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/friend-request/{id}") // POST
    public ResponseEntity<?> addFriendRequest(@PathVariable("id") Long id) {
        friendshipService.friendRequest(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Friend request sent")
                        .data(null)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @DeleteMapping("/cancel-request/{id}") // DELETE
    public ResponseEntity<?> cancelFriendRequest(@PathVariable("id") Long id) {
        friendshipService.cancelFriendRequest(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Friend request has been cancelled.")
                        .data(null)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @DeleteMapping("/unfriend/{id}") // DELETE
    public ResponseEntity<?> unfriend(@PathVariable("id") Long id) {
        friendshipService.unfriend(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Unfriended this user")
                        .data(null)
                        .build()
        );
    }
}