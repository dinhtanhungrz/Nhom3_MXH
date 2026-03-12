package com.be_mxh.service;

import java.util.List;

import com.be_mxh.dto.comment.CommentResponseDisplay;
import com.be_mxh.dto.comment.CommentRequest;
import com.be_mxh.dto.comment.CommentResponse;

import java.util.List;

public interface CommentService {

  /**
   * Đăng comment vào một bài viết (status).
   *
   * @param postId   ID của bài viết cần comment
   * @param content  Nội dung comment
   * @param username Username của người comment
   */
  void comment(Long postId, String content, String username);

  /**
   * Xóa mềm comment (soft-delete: đặt deleted = true).
   * Chỉ chủ comment mới được phép xóa.
   *
   * @param commentId     ID của comment cần xóa
   * @param currentUserId ID người đang đăng nhập (phải là chủ comment)
   * @throws ResourceNotFoundException nếu comment không tồn tại hoặc đã bị xóa
   * @throws AccessDeniedException     nếu không phải chủ comment
   */
  void deleteComment(Long commentId, Long currentUserId);

  /**
   * Lấy danh sách comment của bài viết
   */
  List<CommentResponseDisplay> getCommentsByPostId(Long postId, Long currentUserId);

  void createComment(CommentRequest request, Long userId);

  List<CommentResponse> getCommentsByStatus(Long statusId, Long currentUserId);

  void updateComment(Long commentId, String content, Long userId);
}
