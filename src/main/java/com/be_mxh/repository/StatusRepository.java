package com.be_mxh.repository;

import com.be_mxh.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StatusRepository extends JpaRepository<Status, Long> {

    List<Status> findByActiveTrueOrderByCreatedAtDesc();

    @Query("""
      SELECT p FROM Status p
      WHERE p.active = false
      AND p.user.id IN (
        SELECT f.following.id FROM Follow f
        WHERE f.follower.id = :userId
      )
      ORDER BY p.createdAt DESC
    """)
    List<Status> feedByFollow(Long userId);
}
