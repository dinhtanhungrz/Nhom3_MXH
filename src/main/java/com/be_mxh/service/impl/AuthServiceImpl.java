package com.be_mxh.service.impl;

import com.be_mxh.dto.auth.LoginRequest;
import com.be_mxh.dto.auth.LoginResponse;
import com.be_mxh.dto.auth.RegisterRequest;
import com.be_mxh.dto.auth.RegisterResponse;
import com.be_mxh.entity.RefreshToken;
import com.be_mxh.entity.Role;
import com.be_mxh.entity.User;
import com.be_mxh.exception.BadRequestException;
import com.be_mxh.repository.RefreshTokenRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.AuthService;
import com.be_mxh.service.RefreshTokenService;
import com.be_mxh.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
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
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        User user = userRepository.findByUsername(loginRequest.getUsername());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtService.generateTokenLogin(authentication);
        RefreshToken refreshToken = refreshTokenService.create(user);
        return new LoginResponse("bearer", accessToken, refreshToken.getToken());
    }

    // mapper
    public User mapToEntity(RegisterRequest req) {
        return User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .avatarUrl(AVATAR_DEFAULT_URL)
                .build();
    }


    private RegisterResponse mapToDto(User savedUser) {
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .roles(savedUser.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

}
