package com.be_mxh.dto.client.auth;

@AllArgsConstructor
@Getter
public class LoginResponse {
    private String token;
    private String tokenType;
    private UserInfo user;
}
