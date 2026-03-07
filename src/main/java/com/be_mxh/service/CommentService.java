package com.be_mxh.service;

import com.be_mxh.dto.comment.CommentRequest;
import com.be_mxh.dto.comment.CommentResponse;

import java.util.List;

public interface CommentService {
  void createComment(CommentRequest request, Long userId);

  List<CommentResponse> getCommentsByStatus(Long statusId, Long currentUserId);

  void updateComment(Long commentId, String content, Long userId);

  void likeComment(Long commentId, Long userId);
  void unlikeComment(Long commentId, Long userId);
  long getCommentLikeCount(Long commentId);

}
