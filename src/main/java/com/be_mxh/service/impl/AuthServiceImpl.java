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
import com.be_mxh.service.GoogleTokenVerifier;
import com.be_mxh.service.RefreshTokenService;
import com.be_mxh.service.RoleService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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
    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

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
    @Override
    public GoogleLoginResponse loginWithGoogle(String idTokenStr) {
        try {
            GoogleIdToken googleIdToken = googleTokenVerifier.verify(idTokenStr);

            if (googleIdToken == null) {
                throw new BadRequestException("Xác thực Google thất bại");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();

            log.info("Google login xử lý cho email: {}", email);

            // 2. Upsert User
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createGoogleUser(email,
                            (String) payload.get("name"),
                            (String) payload.get("picture")));

            // 3. Xử lý Roles (Sửa lỗi Stream address)
            Set<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            // 4. Sinh cặp Token
            // Chú ý: Dùng hàm generateToken(user) từ file JWTService.java bạn đã gửi
            String accessToken = jwtService.generateToken(user);

            // Sinh Refresh Token từ RefreshTokenService của bạn
            RefreshToken refreshTokenObj = refreshTokenService.create(user);

            // 5. Fix lỗi Builder: Đảm bảo field name trong DTO khớp với builder
            return GoogleLoginResponse.builder()
                    .accessToken(accessToken) // Khớp với field accessToken trong DTO
                    .refreshToken(refreshTokenObj.getToken())
                    .role(roles)             // Truyền trực tiếp Set<String>
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .build();

        } catch (Exception e) {
            log.error("🔥 GOOGLE LOGIN ERROR: ", e);
            throw new BadRequestException("Lỗi hệ thống khi đăng nhập Google");
        }
    }
    private User createGoogleUser(String email, String name, String picture) {
        log.info("Creating Google user for email: {}", email);

        String username = generateUniqueUsername(email);

        Role role = roleService.findByName("ROLE_USER");
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .email(email)
                .username(username)
                .password("") // Google login không cần password
                .fullName(name)
                .avatarUrl(picture != null ? picture : AVATAR_DEFAULT_URL)
                .enabled(true)
                .status(User.UserStatus.PUBLIC)
                .displayFriendsStatus(User.DisplayFriendsStatus.PUBLIC)
                .roles(roles)
                .build();

        return userRepository.save(user);
    }
    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0];
        String username = base;
        int i = 1;

        while (userRepository.existsByUsername(username)) {
            username = base + i;
            i++;
        }

        return username;
    }

}
