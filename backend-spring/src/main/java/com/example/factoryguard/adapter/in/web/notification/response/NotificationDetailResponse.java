package com.example.factoryguard.adapter.in.web.notification.response;

import com.example.factoryguard.application.dto.notification.NotificationDetail;
import com.example.factoryguard.domain.notification.vo.NotificationSeverity;
import com.example.factoryguard.domain.notification.vo.NotificationType;

public record NotificationDetailResponse(
        Long notificationId,
        NotificationType notificationType,
        NotificationSeverity severity,
        String title,
        String message,
        String targetUrl,
        Boolean isRead
) {
    public static NotificationDetailResponse from(NotificationDetail detail) {
        return new NotificationDetailResponse(
                detail.getNotificationId(),
                detail.getNotificationType(),
                detail.getSeverity(),
                detail.getTitle(),
                detail.getMessage(),
                detail.getTargetUrl(),
                detail.getIsRead()
        );
    }
}
