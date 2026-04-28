package com.example.factoryguard.application.dto.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListNotificationsQuery {

    private final Long userId;
    private final Boolean isRead;
    private final int page;
    private final int size;
}
