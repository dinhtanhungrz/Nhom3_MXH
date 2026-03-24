package com.be_mxh.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(
                        name = "idx_notify_receiver_read_created",
                        columnList = "receiver_id, read_at, created_at"
                )
        }
)
@Setter
@Getter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // Người tạo hành động (có thể null: system/admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    // Loại thông báo (friend, like, comment, ...)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    // Kiểu entity liên quan (POST, COMMENT, USER...)
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private EntityType entityType;

    // ID entity liên quan
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    // Thời điểm đọc (null = chưa đọc)
    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Thời điểm tạo thông báo
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =========================
    // Lifecycle
    // =========================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // =========================
    // Business helpers
    // =========================
    public boolean isRead() {
        return readAt != null;
    }

    public void markAsRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    // enums
    public enum NotificationType {
        FRIEND_REQUEST,
        FRIEND_ACCEPTED,
        LIKE_STATUS,
        LIKE_COMMENT,
        COMMENT_STATUS,
        REPLY_COMMENT,
        MENTION,
        SYSTEM
    }

    public enum EntityType {
        USER,
        POST,
        COMMENT
    }

}