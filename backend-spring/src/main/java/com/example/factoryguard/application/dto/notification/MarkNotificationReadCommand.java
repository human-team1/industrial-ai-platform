package com.example.factoryguard.application.dto.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MarkNotificationReadCommand {

    private final Long userId;
    private final Long notificationId;
}
