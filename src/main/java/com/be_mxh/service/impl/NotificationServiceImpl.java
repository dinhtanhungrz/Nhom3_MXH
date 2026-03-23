package com.be_mxh.service.impl;

import com.be_mxh.dto.user.NotificationResponse;
import com.be_mxh.entity.Notification;
import com.be_mxh.entity.User;
import com.be_mxh.exception.BadRequestException;
import com.be_mxh.repository.NotificationRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.AuthService;
import com.be_mxh.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;


    @Override
    public List<NotificationResponse> getMyNotifications(Pageable pageable) {
        Long userId = authService.getCurrentUserId();

        return notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(userId, pageable)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public long countUnread() {
        Long userId = authService.getCurrentUserId();
        return notificationRepository.countByReceiverIdAndReadAtIsNull(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BadRequestException("Notification not found"));

        if (!notification.getReceiver().getId().equals(authService.getCurrentUserId())) {
            throw new BadRequestException("Access denied");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead() {
        Long userId = authService.getCurrentUserId();

        List<Notification> list = notificationRepository
                .findByReceiverIdOrderByCreatedAtDesc(userId, Pageable.unpaged());

        list.forEach(Notification::markAsRead);
        notificationRepository.saveAll(list);
    }

    @Override
    public void createNotification(Long receiverId, Long actorId, String type, String entityType, Long entityId) {
        if (receiverId.equals(actorId)) return; // tránh tự notify mình

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new BadRequestException("Receiver not found"));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new BadRequestException("Actor not found"));

        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setActor(actor);
        notification.setType(Notification.NotificationType.valueOf(type));
        notification.setEntityType(Notification.EntityType.valueOf(entityType));
        notification.setEntityId(entityId);

        notificationRepository.save(notification);
    }
    private NotificationResponse mapToDto(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .actorId(n.getActor() != null ? n.getActor().getId() : null)
                .actorName(n.getActor() != null ? n.getActor().getUsername() : "System")
                .actorAvatar(n.getActor() != null ? n.getActor().getAvatarUrl() : null)
                .type(n.getType())
                .entityType(n.getEntityType())
                .entityId(n.getEntityId())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
