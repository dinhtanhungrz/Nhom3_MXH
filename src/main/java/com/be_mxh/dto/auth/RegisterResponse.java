package com.be_mxh.dto.auth;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDate dateOfBirth;
    private String phone;
    private Set<String> roles;
    private LocalDateTime createdAt;
}
