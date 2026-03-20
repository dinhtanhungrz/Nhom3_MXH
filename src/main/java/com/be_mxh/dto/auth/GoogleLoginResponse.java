package com.be_mxh.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class GoogleLoginResponse {
    private String accessToken; // JWT của hệ thống bạn
    private String refreshToken;
    private Set<String> role;
    private String email;
    private String username;
}
