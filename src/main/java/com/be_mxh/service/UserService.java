package com.be_mxh.service;

import com.be_mxh.dto.user.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService extends UserDetailsService {

    ProfileResponse getProfile();

    ProfileResponse updateProfile(ProfileRequest profileRequest);

    void updatePassword(UpdatePasswordRequest updatePasswordRequest);

    List<UserResponse> getAllUsers();

    UserResponse blockUser(Long id);

    UserProfileResponse getUserProfile(Long profileUserId);

  String updateAvatar(MultipartFile avatar);
}
