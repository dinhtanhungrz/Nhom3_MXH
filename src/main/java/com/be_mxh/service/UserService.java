package com.be_mxh.service;

import com.be_mxh.dto.client.auth.RegisterRequest;
import com.be_mxh.dto.client.auth.RegisterResponse;
import com.be_mxh.dto.user.UpdatePasswordRequest;
import com.be_mxh.dto.user.UpdateProfileRequest;
import com.be_mxh.dto.user.UserProfileResponse;
import com.be_mxh.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {
    RegisterResponse save(RegisterRequest registerRequest);

    Iterable<User> findAll();

    User findByUsername(String username);

    UserProfileResponse getProfile();

    UserProfileResponse getPublicProfileByUsername(String username);

    UserProfileResponse updateProfile(UpdateProfileRequest updateProfileRequest);

    Optional<User> findById(Long id);

    UserDetails loadUserById(Long id);

    boolean checkLogin(User user);

    boolean isDuplicateUsername(String username);

    boolean isDuplicateEmail(String email);

    boolean isCorrectConfirmPassword(String password, String confirmPassword);

    void updatePassword(UpdatePasswordRequest updatePasswordRequest);
}