package com.be_mxh.dto.user;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private String avatarUrl;
    private LocalDateTime dateOfBirth;
    private String gender;
    private String hobby;
}