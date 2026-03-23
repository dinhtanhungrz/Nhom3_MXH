package com.be_mxh.service;

import com.be_mxh.dto.user.NotificationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications(Pageable pageable);

    long countUnread();

    void markAsRead(Long notificationId);

    void markAllAsRead();

    void createNotification(
            Long receiverId,
            Long actorId,
            String type,
            String entityType,
            Long entityId
    );
}
