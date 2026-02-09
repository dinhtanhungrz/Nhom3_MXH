package com.be_mxh.dto.user;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
public class ProfileRequest {
    private MultipartFile avatar;

    @NotBlank(message = "Full name is required")
    @Pattern(
            regexp = "^[a-zA-ZÀ-ỹ\\s]+$",
            message = "Full name must not contain special characters"
    )
    private String fullName;

    @Pattern(
            regexp = "^[a-zA-Z0-9À-ỹ\\s]*$",
            message = "Address must not contain special characters"
    )
    private String address;

    @Pattern(
            regexp = "^$|^(0|\\+84)[0-9]{9}$",
            message = "Invalid phone number"
    )
    private String phone;

    @Pattern(
            regexp = "^[a-zA-Z0-9À-ỹ\\s,]*$",
            message = "Hobby must not contain special characters"
    )
    private String hobby;
}
