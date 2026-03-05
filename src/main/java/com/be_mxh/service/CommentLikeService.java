package com.be_mxh.service;

public interface CommentLikeService {

    /**
     * Like comment
     */
    void likeComment(Long commentId, String username);

    /**
     * Unlike comment
     */
    void unlikeComment(Long commentId, String username);

    /**
     * Lấy số lượng like của comment
     */
    long getLikeCount(Long commentId);

    /**
     * Kiểm tra user đã like comment chưa
     */
    boolean isCommentLikedByUser(Long commentId, String username);
}
