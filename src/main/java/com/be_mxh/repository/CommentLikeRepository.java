package com.be_mxh.repository;

import com.be_mxh.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);
    List<CommentLike> findByCommentId(Long commentId);
    long countByCommentId(Long commentId);
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
    // Để kiểm tra trạng thái Like (Dùng cho nút màu xanh)
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
}