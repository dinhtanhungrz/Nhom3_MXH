package com.be_mxh.dto.status;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusResponseDisplay {
    private Long id;
    private String content;
    private String visibility;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private long likesCount;
    private long commentCount;

    private boolean canComment;

    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;

    private List<String> imageUrls;
}
