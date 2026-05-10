package com.example.factoryguard.adapter.out.persistence.notification;

import com.example.factoryguard.application.dto.notification.NotificationSummary;
import com.example.factoryguard.domain.notification.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationPersistenceMapper {

    public Notification toDomain(NotificationJpaEntity entity) {
        return Notification.builder()
                .notificationId(entity.getNotificationId())
                .userId(entity.getUserId())
                .notificationType(entity.getNotificationType())
                .severity(entity.getSeverity())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .relatedType(entity.getRelatedType())
                .relatedId(entity.getRelatedId())
                .targetUrl(entity.getTargetUrl())
                .dedupKey(entity.getDedupKey())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public NotificationJpaEntity toEntity(Notification notification) {
        return NotificationJpaEntity.builder()
                .userId(notification.getUserId())
                .notificationType(notification.getNotificationType())
                .severity(notification.getSeverity())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedType(notification.getRelatedType())
                .relatedId(notification.getRelatedId())
                .targetUrl(notification.getTargetUrl())
                .dedupKey(notification.getDedupKey())
                .isRead(notification.getIsRead())
                .build();
    }

    public NotificationSummary toSummary(NotificationJpaEntity entity) {
        return NotificationSummary.builder()
                .notificationId(entity.getNotificationId())
                .notificationType(entity.getNotificationType())
                .severity(entity.getSeverity())
                .title(entity.getTitle())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
