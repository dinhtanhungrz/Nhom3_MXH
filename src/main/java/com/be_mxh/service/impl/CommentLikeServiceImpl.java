package com.be_mxh.service.impl;

import com.be_mxh.entity.Comment;
import com.be_mxh.entity.CommentLike;
import com.be_mxh.entity.User;
import com.be_mxh.repository.CommentLikeRepository;
import com.be_mxh.repository.CommentRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.CommentLikeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public void likeComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment không tồn tại"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Optional<CommentLike> existingLike = commentLikeRepository
                .findByCommentIdAndUserId(commentId, user.getId());

        if (existingLike.isEmpty()) {
            CommentLike commentLike = CommentLike.builder()
                    .comment(comment)
                    .user(user)
                    .build();
            commentLikeRepository.save(commentLike);
        }
    }

    @Override
    public void unlikeComment(Long commentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        commentLikeRepository.deleteByCommentIdAndUserId(commentId, user.getId());
    }

    @Override
    public long getLikeCount(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }

    @Override
    public boolean isCommentLikedByUser(Long commentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return commentLikeRepository.findByCommentIdAndUserId(commentId, user.getId())
                .isPresent();
    }
}