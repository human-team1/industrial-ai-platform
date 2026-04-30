package com.example.factoryguard.adapter.out.fastapi.request;

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
