package com.example.factoryguard.application.dto.notification;

import com.example.factoryguard.domain.notification.vo.NotificationSeverity;
import com.example.factoryguard.domain.notification.vo.NotificationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationDetail {

    private final Long notificationId;
    private final NotificationType notificationType;
    private final NotificationSeverity severity;
    private final String title;
    private final String message;
    private final String targetUrl;
    private final Boolean isRead;
}
