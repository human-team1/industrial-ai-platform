package com.example.factoryguard.application.port.in.notification;

import com.example.factoryguard.application.dto.notification.NotificationDetail;

public interface GetNotificationDetailUseCase {

    NotificationDetail execute(Long userId, Long notificationId);
}
