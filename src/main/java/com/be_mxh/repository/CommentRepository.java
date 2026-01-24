package com.be_mxh.repository;

import com.be_mxh.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdAndDeletedFalseOrderByCreatedAtAsc(Long postId);

    long countByPostIdAndDeletedFalse(Long postId);
}
