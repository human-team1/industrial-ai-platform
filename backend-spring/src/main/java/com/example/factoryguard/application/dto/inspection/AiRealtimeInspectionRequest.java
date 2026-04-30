package com.example.factoryguard.application.dto.inspection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AiRealtimeInspectionRequest {

    private final Long cameraId;
    private final String streamUrl;
    private final double anomalyThreshold;
    private final double lowConfidenceThreshold;
}
