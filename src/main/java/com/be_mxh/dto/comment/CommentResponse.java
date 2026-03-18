package com.be_mxh.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isOwner")
    private boolean isOwner; // Để frontend ẩn/hiện nút sửa
    private long likeCount;
    @JsonProperty("isLiked")
    private boolean isLiked;
}
