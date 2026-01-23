package com.be_mxh.dto.client.auth;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class JWTResponse {
    private String tokenType;
    private String accessToken;
}