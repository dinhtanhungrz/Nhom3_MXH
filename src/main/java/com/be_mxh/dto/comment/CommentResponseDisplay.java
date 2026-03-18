package com.be_mxh.dto.comment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDisplay {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private LocalDateTime createdAt;
    private boolean canDelete; // Để FE biết có thể hiện nút Xóa
}
