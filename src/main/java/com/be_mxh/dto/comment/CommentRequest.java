package com.be_mxh.dto.comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private String content;
    private Long statusId;
    private Long parentId; // Có thể null nếu là comment cấp 1
    private String imageUrl; // Cho tương lai nếu cần
}
