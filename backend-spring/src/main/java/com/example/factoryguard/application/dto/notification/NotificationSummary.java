package com.example.factoryguard.application.dto.notification;

import com.example.factoryguard.domain.notification.vo.NotificationSeverity;
import com.example.factoryguard.domain.notification.vo.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationSummary {

    private final Long notificationId;
    private final NotificationType notificationType;
    private final NotificationSeverity severity;
    private final String title;
    private final Boolean isRead;
    private final LocalDateTime createdAt;
}
