package com.example.factoryguard.domain.user.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserSetting {

    private final Long userSettingId;
    private final Long userId;
    private final Boolean notificationEnabled;
    private final String defaultDashboardRange;
    private final Long defaultCameraId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
