package com.be_mxh.repository;

import com.be_mxh.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    long countByReceiverIdAndReadAtIsNull(Long receiverId);

    void deleteByReceiverIdAndActorIdAndTypeAndEntityTypeAndEntityId(Long receiverId, Long actorId, Notification.NotificationType type, Notification.EntityType entityType, Long entityId);

    List<Notification> findAllByEntityIdAndEntityTypeAndTypeIn(
      Long entityId,
      Notification.EntityType entityType,
      List<Notification.NotificationType> types
    );
}
