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
    private Long id;

    private MultipartFile avatar;

    @NotBlank(message = "Firstname is required")
    @Pattern(
            regexp = "^[a-zA-ZÀ-ỹ\\s]+$",
            message = "Full name must not contain special characters"
    )
    private String firstName;

    @NotBlank(message = "Lastname is required")
    @Pattern(
            regexp = "^[a-zA-ZÀ-ỹ\\s]+$",
            message = "Full name must not contain special characters"
    )
    private String lastName;

    @Pattern(
            regexp = "^[a-zA-Z0-9À-ỹ\\s]*$",
            message = "Address must not contain special characters"
    )
    private String address;

    @Pattern(
            regexp = "^[0-9]{9,11}$",
            message = "Phone number is invalid"
    )
    private String phone;

    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;

    @Pattern(
            regexp = "MALE|FEMALE|OTHER",
            message = "Gender must be MALE, FEMALE, or OTHER"
    )
    private String gender;

    @Pattern(
            regexp = "^[a-zA-Z0-9À-ỹ\\s,]*$",
            message = "Hobby must not contain special characters"
    )
    private String hobby;
}
