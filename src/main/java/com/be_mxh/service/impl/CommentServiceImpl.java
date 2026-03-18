package com.be_mxh.service.impl;

import com.be_mxh.dto.comment.CommentRequest;
import com.be_mxh.dto.comment.CommentResponse;
import com.be_mxh.dto.comment.CommentResponseDisplay;
import com.be_mxh.entity.Comment;
import com.be_mxh.entity.Friendship;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.User;
import com.be_mxh.entity.*;
import com.be_mxh.exception.ResourceNotFoundException;
import com.be_mxh.exception.UnauthorizedException;
import com.be_mxh.repository.*;
import com.be_mxh.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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
    return commentRepository.findAllByStatusIdAndDeletedFalseOrderByCreatedAtDesc(statusId)
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

  // =====================================================================
  // XÓA COMMENT
  // =====================================================================

  /**
   * Xóa mềm comment (soft-delete: đặt deleted = true, KHÔNG xóa khỏi DB).
   *
   * Quy trình:
   *  1. Tìm comment theo ID và deleted = false → 404 nếu không tìm thấy
   *  2. So sánh userId của comment với currentUserId → 403 nếu không khớp
   *  3. Đặt deleted = true và lưu DB
   *
   * @param commentId     ID của comment cần xóa
   * @param currentUserId ID của người đang đăng nhập (phải là chủ comment)
   */
  @Override
  public void deleteComment(Long commentId, Long currentUserId) {
    log.info("[CommentService] deleteComment – commentId={}, requestBy userId={}",
      commentId, currentUserId);

    // Bước 1: Tìm comment còn hoạt động (chưa bị xóa mềm)
    Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
      .orElseThrow(() -> {
        log.warn("[CommentService] Comment không tồn tại hoặc đã bị xóa – commentId={}", commentId);
        return new ResourceNotFoundException("Comment không tồn tại hoặc đã bị xóa");
      });

    // Bước 2: Kiểm tra quyền – chỉ chủ comment mới được xóa
    Long ownerId = comment.getUser().getId();
    if (!ownerId.equals(currentUserId)) {
      log.warn("[CommentService] Từ chối xóa – comment thuộc userId={}, request từ userId={}",
        ownerId, currentUserId);
      throw new AccessDeniedException("Bạn không có quyền xóa comment này");
    }

    // Bước 3: Soft-delete
    comment.setDeleted(true);
    commentRepository.save(comment);
    log.info("[CommentService] ✅ Comment {} đã xóa mềm thành công (userId={})",
      commentId, currentUserId);
  }


  // mapper


    @Override
    public boolean toggleLikeComment(Long commentId, Long userId) {
      Comment comment = commentRepository.findById(commentId)
              .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);

      if (existingLike.isPresent()) {
        // Nếu đã like rồi -> Xóa (Unlike)
        commentLikeRepository.delete(existingLike.get());
        return false; // Trả về false nghĩa là hiện tại "Không còn like"
      } else {
        // Nếu chưa like -> Thêm mới (Like)
        CommentLike commentLike = new CommentLike();
        commentLike.setComment(comment);
        commentLike.setUser(user);
        commentLikeRepository.save(commentLike);
        return true; // Trả về true nghĩa là hiện tại "Đã like"
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

  private CommentResponse mapToResponse(Comment comment, Long currentUserId) {
    // 1. Kiểm tra xem người dùng hiện tại đã Like comment này chưa
    boolean isLiked = false;
    if (currentUserId != null) {
      // Gọi repository để check sự tồn tại của bản ghi Like
      isLiked = commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), currentUserId);
    }

    // 2. Đếm tổng số lượt Like của comment này
    long totalLikes = commentLikeRepository.countByCommentId(comment.getId());

    return CommentResponse.builder()
            .id(comment.getId())
            .content(comment.getContent())
            .username(comment.getUser().getUsername())
            .userAvatar(comment.getUser().getAvatarUrl())
            .createdAt(comment.getCreatedAt())
            .isOwner(currentUserId != null && comment.getUser().getId().equals(currentUserId))
            .likeCount(totalLikes) // Thay số 0 bằng biến totalLikes
            .isLiked(isLiked)       // Thay false bằng biến isLiked
            .build();
  }
}

