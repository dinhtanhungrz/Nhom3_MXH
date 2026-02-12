package com.be_mxh.repository;

import com.be_mxh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    @Query(value = "SELECT status FROM mxh.friendships WHERE (requester_id = :u1 AND addressee_id = :u2) OR (requester_id = :u2 AND addressee_id = :u1)", nativeQuery = true)
    String findRelationshipStatus(@Param("u1") Long u1, @Param("u2") Long u2);
}
