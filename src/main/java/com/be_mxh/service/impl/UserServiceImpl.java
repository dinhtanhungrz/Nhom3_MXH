package com.be_mxh.service.impl;

import com.be_mxh.config.security.SecurityUtils;
import com.be_mxh.dto.image.ImageUploadResult;
import com.be_mxh.dto.user.*;
import com.be_mxh.entity.Role;
import com.be_mxh.entity.User;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.exception.BadRequestException;
import com.be_mxh.exception.ResourceNotFoundException;
import com.be_mxh.exception.UnauthorizedException;
import com.be_mxh.repository.FriendshipRepository;
import com.be_mxh.repository.StatusRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.FriendshipService;
import com.be_mxh.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private FriendshipRepository friendshipRepository;
  @Autowired
  private StatusRepository statusRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private ImageUploadServiceImpl imageUploadService;
  @Autowired
  private FriendshipService friendshipService;
  @Value("${AVATAR_DEFAULT_URL}")
  private String AVATAR_DEFAULT_URL;
  @Autowired
  private SecurityUtils securityUtils;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
      .orElseThrow(() ->
        new UsernameNotFoundException("User not found: " + username));

    return UserPrincipal.build(user);
  }


  @Override
  public ProfileResponse getProfile() {
    User user = securityUtils.getCurrentUser();
    return mapToUserInfoDto(user);
  }

  @Override
  public ProfileResponse updateProfile(ProfileRequest req) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException("Unauthenticated");
    }

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    assert userDetails != null;
    String username = userDetails.getUsername();

    // 2️⃣ Load user từ DB
    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    if (req.getAvatar() != null && !req.getAvatar().isEmpty()) {
      if (!user.getAvatarUrl().equals(AVATAR_DEFAULT_URL)) {
        imageUploadService.delete(user.getAvatarUrl());
      }
      ImageUploadResult imageUploadResult = imageUploadService.upload(req.getAvatar(), "avatars");
      user.setAvatarUrl(imageUploadResult.getUrl());
    }
    user.setFullName(req.getFullName());
    user.setAddress(req.getAddress());
    user.setPhone(req.getPhone() != null && req.getPhone().isBlank() ? null : req.getPhone());
    user.setHobby(req.getHobby());
    userRepository.save(user);
    return mapToUserInfoDto(user);
  }

  @Override
  @Transactional
  public void updatePassword(UpdatePasswordRequest request) {
    User user = securityUtils.getCurrentUser();

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new BadRequestException("Password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(request.getPassword()));
    userRepository.save(user);

  }

  @Override
  public List<UserResponse> getAllUsers() {
    List<User> users = userRepository.findAll();

    return users.stream()
      .map(this::mapToUserResponse)
      .toList();
  }

  @Override
  public UserResponse blockUser(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    user.setEnabled(Boolean.FALSE);
    User result = userRepository.save(user);
    return mapToUserResponse(result);
  }

  @Override
  @Transactional(readOnly = true)
  public UserProfileResponse getUserProfile(Long profileUserId) {
    User currentUser = securityUtils.getCurrentUser();
    User user = userRepository.findById(profileUserId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    UserProfileResponse dto = new UserProfileResponse();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setFullName(user.getFullName());
    dto.setAvatarUrl(user.getAvatarUrl());
    dto.setAddress(user.getAddress());
    dto.setPhone(user.getPhone());
    dto.setEmail(user.getEmail());
    dto.setDateOfBirth(user.getDateOfBirth());
    dto.setCreatedAt(user.getCreatedAt());

    // Counts
    dto.setRelationshipStatus(friendshipService.getRelationship(currentUser.getId(), user.getId()));
    dto.setFriendsCount(friendshipRepository.countFriendshipsByUserId(user.getId()));
    dto.setPostsCount(statusRepository.countByUserId(user.getId()));
    return dto;
  }

  @Override
  public String updateAvatar(MultipartFile avatar) {
    User user = securityUtils.getCurrentUser();
    String oldAvatar = user.getAvatarUrl();

    if (oldAvatar != null && !oldAvatar.equals(AVATAR_DEFAULT_URL)) {
      imageUploadService.delete(oldAvatar);
    }

    String newAvatar = imageUploadService.upload(avatar, "avatars").getUrl();
    user.setAvatarUrl(newAvatar);
    userRepository.save(user);
    return newAvatar;
  }

  // mapper

  private ProfileResponse mapToUserInfoDto(User user) {
    return ProfileResponse.builder()
      .id(user.getId())
      .username(user.getUsername())
      .email(user.getEmail())
      .fullName(user.getFullName())
      .phone(user.getPhone())
      .avatarUrl(user.getAvatarUrl())
      .dateOfBirth(user.getDateOfBirth())
      .address(user.getAddress())
      .hobby(user.getHobby())
      .gender(user.getGender() != null ? user.getGender().name() : null)
      .build();
  }

  public UserResponse mapToUserResponse(User user) {
    return UserResponse.builder()
      .id(user.getId())
      .username(user.getUsername())
      .fullName(user.getFullName())
      .email(user.getEmail())
      .phone(user.getPhone())
      .address(user.getAddress())
      .avatarUrl(user.getAvatarUrl())
      .gender(user.getGender())
      .dateOfBirth(user.getDateOfBirth())
      .hobby(user.getHobby())
      .status(user.getStatus())
      .enabled(user.isEnabled())
      .displayFriendsStatus(user.getDisplayFriendsStatus())
      .roles(
        user.getRoles()
          .stream()
          .map(Role::getName)
          .collect(Collectors.toSet())
      )
      .createdAt(user.getCreatedAt())
      .build();
  }
}
