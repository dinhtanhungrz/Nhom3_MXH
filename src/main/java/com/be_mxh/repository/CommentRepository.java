package com.be_mxh.repository;

import com.be_mxh.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByStatusId(Long statusId);
    List<Comment> findAllByStatusIdOrderByCreatedAtDesc(Long statusId);
}

