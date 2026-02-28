package com.be_mxh.dto.status;

import lombok.*;

import java.time.LocalDateTime;
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

    private Integer likesCount;
    private Integer commentsCount;

    private List<String> imageUrls;
}