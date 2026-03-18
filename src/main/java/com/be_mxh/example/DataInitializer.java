package com.be_mxh.example;

import com.be_mxh.entity.Role;
import com.be_mxh.entity.User;
import com.be_mxh.repository.RoleRepository;
import com.be_mxh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@Component
public class DataInitializer implements CommandLineRunner {
  @Value("${AVATAR_DEFAULT_URL}")
  String avatarDefaultUrl;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {

    // 1. Create roles if not exist
    Role roleUser = roleRepository.findByName("ROLE_USER")
      .orElseGet(() -> roleRepository.save(
        new Role(null, "ROLE_USER")
      ));

    Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
      .orElseGet(() -> roleRepository.save(
        new Role(null, "ROLE_ADMIN")
      ));

    // 2. Create admin user if not exist
    if (!userRepository.existsByUsername("admin")) {
      User admin = User.builder()
        .username("admin")
        .email("admin@gmail.com")
        .password(passwordEncoder.encode("123456"))
        .roles(Set.of(roleAdmin))
        .enabled(true)
        .status(User.UserStatus.PUBLIC)
        .fullName("Admin")
        .avatarUrl(avatarDefaultUrl)
        .build();

      userRepository.save(admin);
    }

    // 2. Create user test if not exist
    if (!userRepository.existsByUsername("nguyenvana")) {
      User user = User.builder()
        .username("nguyenvana")
        .email("nguyenvana@gmail.com")
        .password(passwordEncoder.encode("123456"))
        .roles(Set.of(roleUser))
        .enabled(true)
        .status(User.UserStatus.PUBLIC)
        .avatarUrl(avatarDefaultUrl)
        .fullName("Nguyen Van A")
        .build();

      userRepository.save(user);
    }
  }
}
