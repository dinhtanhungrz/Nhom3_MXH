package com.be_mxh.service;

import com.be_mxh.entity.RefreshToken;
import com.be_mxh.entity.User;

public interface RefreshTokenService {
    RefreshToken create(User user);

    RefreshToken verify(String token);

    void revoke(RefreshToken token);
}
