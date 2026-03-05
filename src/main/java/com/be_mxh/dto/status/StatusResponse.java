package com.be_mxh.dto.status;

import com.be_mxh.entity.Status;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusResponse {
    private Long id;
    private String content;
    private String visibility;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long likesCount;
    private Long commentsCount;

    private List<StatusImageResponse> imageUrls = new ArrayList<>();

    public StatusResponse(
            Long id,
            String content,
            Status.Visibility visibility,
            Boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long likesCount,
            Long commentsCount
    ) {
        this.id = id;
        this.content = content;
        this.visibility = visibility.name();
        this.isActive = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }

    public void setImages(List<StatusImageResponse> images) {
        this.imageUrls = images;
    }
}