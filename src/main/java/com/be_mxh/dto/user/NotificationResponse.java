package com.be_mxh.dto.user;

import com.be_mxh.entity.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long actorId;
    private String actorName;
    private String actorAvatar;

    private Notification.NotificationType type;
    private Notification.EntityType entityType;
    private Long entityId;

    private boolean isRead;
    private LocalDateTime createdAt;
}
