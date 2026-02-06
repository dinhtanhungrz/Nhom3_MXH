package com.be_mxh.service.impl;

import com.be_mxh.dto.image.ImageUploadResult;
import com.be_mxh.dto.user.UpdatePasswordRequest;
import com.be_mxh.dto.user.UpdateProfileRequest;
import com.be_mxh.dto.user.UserProfileResponse;
import com.be_mxh.dto.user.UserResponse;
import com.be_mxh.entity.User;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.exception.BadRequestException;
import com.be_mxh.exception.UnauthorizedException;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ImageUploadServiceImpl imageUploadService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        if (this.checkLogin(user)) {
            return UserPrincipal.build(user);
        }
        boolean enable = false;
        boolean accountNonExpired = false;
        boolean credentialsNonExpired = false;
        boolean accountNonLocked = false;
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(), enable, accountNonExpired, credentialsNonExpired,
                accountNonLocked, null);
    }


    @Override
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserProfileResponse getProfile() {
        User user;
        String userName;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            userName = ((UserDetails) principal).getUsername();
        } else {
            userName = principal.toString();
        }
        user = this.findByUsername(userName);
        return mapToUserInfoDto(user);
    }

    @Override
    public UserProfileResponse updateProfile(UpdateProfileRequest req) {
        User user = userRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!req.getAvatar().isEmpty()) {
            if (user.getAvatarUrl() != null)
                imageUploadService.delete(user.getAvatarUrl());
            ImageUploadResult imageUploadResult = imageUploadService.upload(req.getAvatar(), "avatars");
            user.setAvatarUrl(imageUploadResult.getUrl());
        }
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setAddress(req.getAddress());
        user.setPhone(req.getPhone());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setGender(User.Gender.valueOf(req.getGender()));
        user.setHobby(req.getHobby());
        userRepository.save(user);
        return mapToUserInfoDto(user);
    }

    @Override
    public UserProfileResponse findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserInfoDto(user);
    }

    @Override
    public UserDetails loadUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NullPointerException();
        }
        return UserPrincipal.build(user.get());
    }

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        User user = getCurrentUser();

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
        user.setStatus(User.UserStatus.LOCKED);
        User result = userRepository.save(user);
        return mapToUserResponse(result);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserDetails userDetails)) {
            throw new UnauthorizedException("Unauthorized");
        }

        return findByUsername(userDetails.getUsername());
    }


    public boolean checkLogin(User user) {
        Iterable<User> users = this.findAll();
        boolean isCorrectUser = false;
        for (User currentUser : users) {
            if (currentUser.getUsername().equals(user.getUsername()) && user.getPassword().equals(currentUser.getPassword()) && currentUser.isEnabled()) {
                isCorrectUser = true;
                break;
            }
        }
        return isCorrectUser;
    }


    // mapper

    private UserProfileResponse mapToUserInfoDto(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
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
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
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
                                .map(role -> role.getName())
                                .collect(Collectors.toSet())
                )
                .createdAt(user.getCreatedAt())
                .build();
    }
}