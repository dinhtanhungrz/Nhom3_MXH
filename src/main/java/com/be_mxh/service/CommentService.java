package com.be_mxh.service;

import java.util.List;

import com.be_mxh.dto.comment.CommentRequest;
import com.be_mxh.dto.comment.CommentResponse;

public interface CommentService {

  void deleteComment(Long commentId, Long currentUserId);

  void createComment(CommentRequest request, Long userId);

  List<CommentResponse> getCommentsByStatus(Long statusId, Long currentUserId);

  void updateComment(Long commentId, String content, Long userId);

  boolean toggleLikeComment(Long commentId, Long userId);
  void unlikeComment(Long commentId, Long userId);
  long getCommentLikeCount(Long commentId);

}
