package com.be_mxh.dto.client.auth;

@Getter
public class UserInfo {
    private Long id;
    private String username;
    private String email;
    private String role;

    public UserInfo(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}
