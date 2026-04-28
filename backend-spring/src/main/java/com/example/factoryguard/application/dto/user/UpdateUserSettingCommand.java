package com.example.factoryguard.application.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateUserSettingCommand {

    private final Long userId;
    private final String sessionId;
    private final Boolean notificationEnabled;
    private final String defaultDashboardRange;
    private final Long defaultCameraId;
}
