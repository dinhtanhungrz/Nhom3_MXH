package com.be_mxh.service.impl;

import com.be_mxh.dto.user.NotificationResponse;
import com.be_mxh.entity.Notification;
import com.be_mxh.entity.User;
import com.be_mxh.exception.BadRequestException;
import com.be_mxh.repository.NotificationRepository;
import com.be_mxh.repository.UserRepository;
import com.be_mxh.service.AuthService;
import com.be_mxh.service.NotificationService;
import com.be_mxh.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private com.be_mxh.repository.StatusRepository statusRepository;

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

    @Override
    @Transactional
    public void revokeNotification(Long receiverId, Long actorId, String type, String entityType, Long entityId) {
        if (receiverId.equals(actorId)) return; // tránh tự notify mình
        
        notificationRepository.deleteByReceiverIdAndActorIdAndTypeAndEntityTypeAndEntityId(
                receiverId,
                actorId,
                Notification.NotificationType.valueOf(type),
                Notification.EntityType.valueOf(entityType),
                entityId
        );
    }
    private NotificationResponse mapToDto(Notification n) {
        Long postId = null;
        Long commentId = null;
        Long statusOwnerId = null;

        if (n.getEntityType() == Notification.EntityType.POST) {
            postId = n.getEntityId();
            statusOwnerId = statusRepository.findById(postId)
                    .map(s -> s.getUser() != null ? s.getUser().getId() : null)
                    .orElse(null);
        } else if (n.getEntityType() == Notification.EntityType.COMMENT) {
            commentId = n.getEntityId();
            com.be_mxh.entity.Comment comment = commentRepository.findById(commentId).orElse(null);
            if (comment != null && comment.getStatus() != null) {
                postId = comment.getStatus().getId();
                statusOwnerId = comment.getStatus().getUser() != null ? comment.getStatus().getUser().getId() : null;
            }
        }

        return NotificationResponse.builder()
                .id(n.getId())
                .actorId(n.getActor() != null ? n.getActor().getId() : null)
                .actorName(n.getActor() != null ? n.getActor().getUsername() : "System")
                .actorAvatar(n.getActor() != null ? n.getActor().getAvatarUrl() : null)
                .type(n.getType())
                .entityType(n.getEntityType())
                .entityId(n.getEntityId())
                .postId(postId)
                .commentId(commentId)
                .statusOwnerId(statusOwnerId)
                .receiverId(n.getReceiver() != null ? n.getReceiver().getId() : null)
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
