package com.example.factoryguard.application.port.out.notification;

import com.example.factoryguard.domain.notification.model.Notification;

import java.util.List;
import java.util.Optional;

public interface LoadNotificationPort {

    Optional<Notification> findById(Long notificationId);

    List<Notification> findAllByUserId(Long userId);
}
