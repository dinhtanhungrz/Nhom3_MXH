package com.be_mxh.dto.user;

import com.be_mxh.entity.User;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;

    private User.Gender gender;
    private LocalDate dateOfBirth;
    private String hobby;

    private User.UserStatus status;
    private boolean enabled;
    private User.DisplayFriendsStatus displayFriendsStatus;

    private Set<String> roles;

    private LocalDateTime createdAt;


}
