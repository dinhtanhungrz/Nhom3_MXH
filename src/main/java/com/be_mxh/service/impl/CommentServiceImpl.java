package com.be_mxh.service.impl;

import com.be_mxh.dto.comment.CommentRequest;
import com.be_mxh.dto.comment.CommentResponse;
import com.be_mxh.entity.*;
import com.be_mxh.exception.ResourceNotFoundException;
import com.be_mxh.exception.UnauthorizedException;
import com.be_mxh.repository.*;
import com.be_mxh.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final CommentLikeRepository commentLikeRepository;


    @Override
    @Transactional
    public void createComment(CommentRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Status status = statusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));

        // Privacy Check
        if (!canUserComment(user, status)) {
            throw new UnauthorizedException("You do not have permission to comment on this post");
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(user);
        comment.setStatus(status);

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParent(parent);
        }

        commentRepository.save(comment);
    }

    private boolean canUserComment(User user, Status status) {
        if (status.getVisibility() == Status.Visibility.PUBLIC) return true;
        if (status.getUser().getId().equals(user.getId())) return true;
        
        Optional<Friendship> friendship = friendshipRepository.findRelationship(user.getId(), status.getUser().getId());
        if (status.getVisibility() == Status.Visibility.FRIENDS_ONLY) {
            return friendship.isPresent() && friendship.get().getStatus() == Friendship.Status.ACCEPTED;
        }
        
        return false; // ONLY_ME
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByStatus(Long statusId, Long currentUserId) {
        return commentRepository.findAllByStatusIdOrderByCreatedAtDesc(statusId)
                .stream()
                .map(comment -> mapToResponse(comment, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateComment(Long commentId, String content, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not the owner of this comment");
        }

        comment.setContent(content);
        commentRepository.save(comment);
    }

    private CommentResponse mapToResponse(Comment comment, Long currentUserId) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .username(comment.getUser().getUsername())
                .userAvatar(comment.getUser().getAvatarUrl())
                .createdAt(comment.getCreatedAt())
                .isOwner(currentUserId != null && comment.getUser().getId().equals(currentUserId))
                .likeCount(0)
                .isLiked(false)
                .build();
    }

    @Override
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Kiểm tra nếu chưa like thì thêm mới
        if (!commentLikeRepository.findByCommentIdAndUserId(commentId, userId).isPresent()) {
            CommentLike commentLike = new CommentLike();
            commentLike.setComment(comment);
            commentLike.setUser(user);
            commentLikeRepository.save(commentLike);
        }
    }

    @Override
    public void unlikeComment(Long commentId, Long userId) {
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
    }

    @Override
    public long getCommentLikeCount(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }

}
