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
    if (username == null || username.isBlank()) {
      throw new UsernameNotFoundException("Username is null or empty");
    }
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
  @Transactional // Nên có transactional để đảm bảo tính toàn vẹn khi upload/save
  public ProfileResponse updateProfile(ProfileRequest req) {
    // 1. Lấy user hiện tại một cách an toàn thông qua SecurityUtils
    // Hàm này của bạn thường đã ném UnauthorizedException nếu chưa login
    User user = securityUtils.getCurrentUser();

    // 2. Xử lý Avatar (Gộp chung logic vào một chỗ)
    if (req.getAvatar() != null && !req.getAvatar().isEmpty()) {

      // Kiểm tra và xóa ảnh cũ nếu không phải ảnh mặc định
      // Đảo ngược equals để tránh NullPointerException nếu AVATAR_DEFAULT_URL null
      if (user.getAvatarUrl() != null && !AVATAR_DEFAULT_URL.equals(user.getAvatarUrl())) {
        imageUploadService.delete(user.getAvatarUrl());
      }
      // Upload ảnh mới
      ImageUploadResult imageUploadResult = imageUploadService.upload(req.getAvatar(), "avatars");
      user.setAvatarUrl(imageUploadResult.getUrl());
    }
    // 3. Cập nhật các thông tin khác
    user.setFullName(req.getFullName());
    user.setAddress(req.getAddress());

    // Xử lý số điện thoại: nếu rỗng thì set null
    String phone = (req.getPhone() != null && req.getPhone().isBlank()) ? null : req.getPhone();
    user.setPhone(phone);

    user.setHobby(req.getHobby());

    // 4. Lưu và trả về kết quả
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
