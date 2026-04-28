package com.example.factoryguard.application.port.in.notification;

import com.example.factoryguard.application.dto.notification.ListNotificationsQuery;
import com.example.factoryguard.application.dto.notification.NotificationSummary;

import java.util.List;

public interface ListNotificationsUseCase {

    List<NotificationSummary> execute(ListNotificationsQuery query);
}
