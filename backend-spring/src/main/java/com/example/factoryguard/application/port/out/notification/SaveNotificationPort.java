package com.example.factoryguard.application.port.out.notification;

import com.example.factoryguard.domain.notification.model.Notification;

public interface SaveNotificationPort {

    Notification save(Notification notification);
}
