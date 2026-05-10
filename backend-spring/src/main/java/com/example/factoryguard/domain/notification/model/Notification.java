package com.example.factoryguard.domain.notification.model;

import com.example.factoryguard.domain.notification.vo.NotificationSeverity;
import com.example.factoryguard.domain.notification.vo.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Notification {

    private final Long notificationId;
    private final Long userId;
    private final NotificationType notificationType;
    private final NotificationSeverity severity;
    private final String title;
    private final String message;
    private final String relatedType;
    private final Long relatedId;
    private final String targetUrl;
    private final String dedupKey;
    private final Boolean isRead;
    private final LocalDateTime createdAt;
}
