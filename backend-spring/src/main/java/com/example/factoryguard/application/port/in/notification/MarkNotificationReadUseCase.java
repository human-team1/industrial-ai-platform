package com.example.factoryguard.application.port.in.notification;

import com.example.factoryguard.application.dto.notification.MarkNotificationReadCommand;

public interface MarkNotificationReadUseCase {

    void execute(MarkNotificationReadCommand command);
}
