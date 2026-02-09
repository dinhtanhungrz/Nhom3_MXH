package com.be_mxh.dto.user;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String address;
    private String phone;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private String hobby;
}