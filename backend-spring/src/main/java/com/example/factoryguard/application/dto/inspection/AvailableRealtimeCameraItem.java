package com.example.factoryguard.application.dto.inspection;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailableRealtimeCameraItem {

    private final Long cameraId;
    private final String cameraName;
    private final Long organizationId;
    private final Long targetId;
    private final String targetName;
    private final String status;
    private final String displayName;
}
