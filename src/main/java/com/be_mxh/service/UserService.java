package com.be_mxh.service;

import com.be_mxh.dto.user.UpdatePasswordRequest;
import com.be_mxh.dto.user.UpdateProfileRequest;
import com.be_mxh.dto.user.UserProfileResponse;
import com.be_mxh.dto.user.UserResponse;
import com.be_mxh.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    Iterable<User> findAll();

    User findByUsername(String username);

    UserProfileResponse getProfile();

    UserProfileResponse updateProfile(UpdateProfileRequest updateProfileRequest);

    UserProfileResponse findById(Long id);

    UserDetails loadUserById(Long id);

    void updatePassword(UpdatePasswordRequest updatePasswordRequest);

    List<UserResponse> getAllUsers();

    UserResponse blockUser(Long id);
}