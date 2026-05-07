package com.example.factoryguard.application.dto.inspection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubmitRealtimeInspectionCommand {

    private final Long userId;
    private final String sessionId;
    private final Long targetId;
    private final Long cameraId;
    private final Long deploymentId;
    private final Long thresholdId;
}
