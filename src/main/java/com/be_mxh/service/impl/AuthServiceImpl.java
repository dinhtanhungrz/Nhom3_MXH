package com.be_mxh.service.impl;

import com.be_mxh.dto.auth.*;
import com.be_mxh.entity.RefreshToken;
import com.be_mxh.entity.Role;
import com.be_mxh.entity.User;
import com.be_mxh.entity.UserPrincipal;
import com.be_mxh.exception.BadRequestException;
import com.be_mxh.repository.RefreshTokenRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.AuthService;
import com.be_mxh.service.RefreshTokenService;
import com.be_mxh.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Value("${AVATAR_DEFAULT_URL}")
    private String AVATAR_DEFAULT_URL;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Override
    public boolean isDuplicateUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isDuplicateEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        User user = mapToEntity(registerRequest);
        if (registerRequest.getRole() == null || registerRequest.getRole().isEmpty()) {
            Role role = roleService.findByName("ROLE_USER");
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
        } else {
            if (registerRequest.getRole().equals("ROLE_ADMIN")) {
                throw new BadRequestException("Admin role is not allowed");
            }
            Role role = roleService.findByName(registerRequest.getRole());
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
        }
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Override
    public RefreshTokenResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new BadRequestException("Refresh token expired");
        }

        User user = token.getUser();

        String newAccessToken = jwtService.generateToken(user);

        return new RefreshTokenResponse(
                newAccessToken,
                "Bearer"
        );
    }

    @Override
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        refreshTokenRepository.delete(token);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtService.generateTokenLogin(authentication);
        RefreshToken refreshToken = refreshTokenService.create(user);
        return new LoginResponse("bearer", user.getRoles().stream().map(Role::getName).toList(), accessToken, refreshToken.getToken());
    }

    // mapper
    public User mapToEntity(RegisterRequest req) {
        return User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .dateOfBirth(req.getDateOfBirth())
                .phone(req.getPhone() != null && req.getPhone().isBlank() ? null : req.getPhone())
                .avatarUrl(AVATAR_DEFAULT_URL)
                .build();
    }


    private RegisterResponse mapToDto(User savedUser) {
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .dateOfBirth(savedUser.getDateOfBirth())
                .phone(savedUser.getPhone())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .roles(savedUser.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(savedUser.getCreatedAt())
                .build();
    }
    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("User not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }

}
