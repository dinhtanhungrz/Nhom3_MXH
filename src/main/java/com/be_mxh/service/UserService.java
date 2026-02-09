package com.be_mxh.service;

import com.be_mxh.dto.user.*;
import com.be_mxh.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    Iterable<User> findAll();

    User findByUsername(String username);

    ProfileResponse getProfile();

    ProfileResponse updateProfile(ProfileRequest profileRequest);

    ProfileResponse findById(Long id);

    User getCurrentUser();

    void updatePassword(UpdatePasswordRequest updatePasswordRequest);

    List<UserResponse> getAllUsers();

    UserResponse blockUser(Long id);

    UserProfileResponse getUserProfile(Long profileUserId);
}