package com.example.factoryguard.application.port.out.notification;

import com.example.factoryguard.application.dto.notification.ListNotificationsQuery;
import com.example.factoryguard.application.dto.notification.NotificationPageResult;
import com.example.factoryguard.domain.notification.model.Notification;

import java.util.Optional;

public interface LoadNotificationPort {

    NotificationPageResult search(ListNotificationsQuery query);

    Optional<Notification> findById(Long notificationId);

    boolean existsByUserIdAndDedupKey(Long userId, String dedupKey);
}
