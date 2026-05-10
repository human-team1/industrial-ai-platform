package com.example.factoryguard.adapter.in.web.user.dto;

import com.example.factoryguard.application.dto.user.UpdateUserSettingCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PatchMySettingRequest {

    private Boolean notificationEnabled;
    private String defaultDashboardRange;
    private Long defaultCameraId;

    public UpdateUserSettingCommand toCommand(Long userId, String sessionId) {
        return new UpdateUserSettingCommand(
                userId,
                sessionId,
                notificationEnabled,
                defaultDashboardRange,
                defaultCameraId
        );
    }
}
