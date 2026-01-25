package com.be_mxh.controller.user;

import com.be_mxh.dto.ApiResponse;
import com.be_mxh.dto.user.UpdatePasswordRequest;
import com.be_mxh.dto.user.UpdateProfileRequest;
import com.be_mxh.dto.user.UserProfileResponse;
import com.be_mxh.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/users")
public class UserRestController {
    @Autowired
    private UserService userService;

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

}