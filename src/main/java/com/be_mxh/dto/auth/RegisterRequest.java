package com.be_mxh.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "First Name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last Name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 32, message = "Password must be between 6 and 32 characters")
    private String password;

    @NotBlank(message = "Confirm Password is required")
    @Size(min = 6, max = 100)
    private String confirmPassword;

    private String role;
}
