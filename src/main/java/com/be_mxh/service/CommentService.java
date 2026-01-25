package com.be_mxh.service;

public interface CommentService {
    void comment(Long postId, String content, String username);
}
