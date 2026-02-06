package com.be_mxh.dto.user;

import com.be_mxh.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProfileRequest {
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
