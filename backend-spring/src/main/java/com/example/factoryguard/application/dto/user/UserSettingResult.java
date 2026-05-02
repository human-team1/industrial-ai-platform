package com.example.factoryguard.application.dto.user;

import com.example.factoryguard.domain.user.model.UserSetting;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSettingResult {

    public static final String DEFAULT_DASHBOARD_RANGE = "7d";
    public static final boolean DEFAULT_NOTIFICATION_ENABLED = true;

    private final Long userSettingId;
    private final Long userId;
    private final Boolean notificationEnabled;
    private final String defaultDashboardRange;
    private final Long defaultCameraId;

    public static UserSettingResult fromUserSetting(UserSetting userSetting) {
        return UserSettingResult.builder()
                .userSettingId(userSetting.getUserSettingId())
                .userId(userSetting.getUserId())
                .notificationEnabled(userSetting.getNotificationEnabled())
                .defaultDashboardRange(userSetting.getDefaultDashboardRange())
                .defaultCameraId(userSetting.getDefaultCameraId())
                .build();
    }

    public static UserSettingResult defaultFor(Long userId) {
        return UserSettingResult.builder()
                .userSettingId(null)
                .userId(userId)
                .notificationEnabled(DEFAULT_NOTIFICATION_ENABLED)
                .defaultDashboardRange(DEFAULT_DASHBOARD_RANGE)
                .defaultCameraId(null)
                .build();
    }
}
