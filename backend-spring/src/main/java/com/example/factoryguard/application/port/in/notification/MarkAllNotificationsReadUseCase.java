package com.example.factoryguard.application.port.in.notification;

import com.example.factoryguard.application.dto.notification.MarkAllReadResult;

public interface MarkAllNotificationsReadUseCase {

    MarkAllReadResult execute(Long userId);
}
