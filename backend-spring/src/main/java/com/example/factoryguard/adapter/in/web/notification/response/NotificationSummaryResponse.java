package com.example.factoryguard.adapter.in.web.notification.response;

import com.example.factoryguard.application.dto.notification.NotificationSummary;
import com.example.factoryguard.domain.notification.vo.NotificationSeverity;
import com.example.factoryguard.domain.notification.vo.NotificationType;

import java.time.LocalDateTime;

public record NotificationSummaryResponse(
        Long notificationId,
        NotificationType notificationType,
        NotificationSeverity severity,
        String title,
        Boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationSummaryResponse from(NotificationSummary summary) {
        return new NotificationSummaryResponse(
                summary.getNotificationId(),
                summary.getNotificationType(),
                summary.getSeverity(),
                summary.getTitle(),
                summary.getIsRead(),
                summary.getCreatedAt()
        );
    }
}
