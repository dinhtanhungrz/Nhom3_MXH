package com.be_mxh.dto.chat;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO cho Message
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private Long id;
    private Long chatGroupId;
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean edited;
    private boolean deleted;
}
