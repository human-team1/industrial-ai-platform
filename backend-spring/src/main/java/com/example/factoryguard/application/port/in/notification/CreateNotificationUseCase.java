package com.example.factoryguard.application.port.in.notification;

import com.example.factoryguard.application.dto.notification.CreateNotificationCommand;
import com.example.factoryguard.domain.notification.model.Notification;

import java.util.Optional;

public interface CreateNotificationUseCase {

    /**
     * 알림을 생성합니다. dedupKey가 존재하고 동일 user_id + dedup_key 알림이 이미 있으면 생성을 건너뜁니다.
     *
     * @return 새로 생성된 경우 알림, 중복으로 건너뛴 경우 Optional.empty()
     */
    Optional<Notification> execute(CreateNotificationCommand command);
}
