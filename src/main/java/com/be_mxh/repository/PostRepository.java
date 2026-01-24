package com.be_mxh.repository;

import com.be_mxh.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByDeletedFalseOrderByCreatedAtDesc();

    @Query("""
      SELECT p FROM Post p
      WHERE p.deleted = false
      AND p.user.id IN (
        SELECT f.following.id FROM Follow f
        WHERE f.follower.id = :userId
      )
      ORDER BY p.createdAt DESC
    """)
    List<Post> feedByFollow(Long userId);
}
