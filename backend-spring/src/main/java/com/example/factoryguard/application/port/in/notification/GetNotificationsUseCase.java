package com.example.factoryguard.application.port.in.notification;

import com.example.factoryguard.application.dto.notification.ListNotificationsQuery;
import com.example.factoryguard.application.dto.notification.NotificationPageResult;

public interface GetNotificationsUseCase {

    NotificationPageResult execute(ListNotificationsQuery query);
}
