package com.be_mxh.repository;

import com.be_mxh.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository
        extends JpaRepository<Like, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);
}
