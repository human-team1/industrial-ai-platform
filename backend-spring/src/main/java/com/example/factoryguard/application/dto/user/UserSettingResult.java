package com.example.factoryguard.application.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSettingResult {

    private final Long userSettingId;
    private final Long userId;
    private final Boolean notificationEnabled;
    private final String defaultDashboardRange;
    private final Long defaultCameraId;
}
