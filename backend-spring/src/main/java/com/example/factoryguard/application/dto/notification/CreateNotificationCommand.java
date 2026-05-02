package com.example.factoryguard.application.dto.notification;

import com.example.factoryguard.domain.notification.vo.NotificationSeverity;
import com.example.factoryguard.domain.notification.vo.NotificationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateNotificationCommand {

    private final Long userId;
    private final NotificationType notificationType;
    private final NotificationSeverity severity;
    private final String title;
    private final String message;
    private final String relatedType;
    private final Long relatedId;
    private final String targetUrl;
    private final String dedupKey;
}
