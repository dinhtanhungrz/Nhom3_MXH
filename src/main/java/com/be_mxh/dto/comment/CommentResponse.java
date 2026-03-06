package com.be_mxh.dto.comment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String username;
    private String userAvatar;
    private String imageUrl;
    private LocalDateTime createdAt;
    private boolean isOwner; // Để frontend ẩn/hiện nút sửa
    private long likeCount;
    private boolean isLiked;
}
