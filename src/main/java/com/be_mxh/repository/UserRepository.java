package com.be_mxh.repository;

import com.be_mxh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findUserByUsername(String username);

  // Đếm user đăng ký trong khoảng thời gian
  @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end")
  Long countByCreatedAtBetween(
    @Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end
  );
}
