package com.be_mxh.service.impl;

import com.be_mxh.entity.Comment;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.User;
import com.be_mxh.exception.ResourceNotFoundException;
import com.be_mxh.repository.CommentRepository;
import com.be_mxh.repository.StatusRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.CommentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;

    // =====================================================================
    // ĐĂNG COMMENT
    // =====================================================================

    /**
     * Đăng một comment mới vào status.
     * Kiểm tra status phải đang active (chưa bị xóa mềm).
     */
    @Override
    public void comment(Long postId, String content, String username) {
        log.info("[CommentService] comment – postId={}, user={}", postId, username);

        Status post = statusRepository.findById(postId)
                .filter(Status::isActive)
                .orElseThrow(() -> {
                    log.warn("[CommentService] Status không tồn tại hoặc đã xóa – postId={}", postId);
                    return new ResourceNotFoundException("Bài viết không tồn tại");
                });

        User user = userRepository
                .findByUsernameOrEmail(username, username)
                .orElseThrow(() -> {
                    log.warn("[CommentService] Không tìm thấy user – username={}", username);
                    return new ResourceNotFoundException("Người dùng không tồn tại");
                });

        Comment c = new Comment();
        c.setContent(content);
        c.setUser(user);
        c.setStatus(post);

        commentRepository.save(c);
        log.info("[CommentService] Đã lưu comment mới – commentId={}, postId={}, userId={}",
                c.getId(), postId, user.getId());
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
}

