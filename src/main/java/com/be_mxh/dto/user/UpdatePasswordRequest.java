package com.be_mxh.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordRequest {
    @NotBlank(message = "Current Password is required")
    private String currentPassword;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 32, message = "Password must be between 6 and 32 characters")
    private String password;

    @NotBlank(message = "Confirm Password is required")
    @Size(min = 6, max = 100)
    private String confirmPassword;
}
