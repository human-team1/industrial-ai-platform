package com.example.factoryguard.adapter.in.web.inspection.dto;

public record SubmitRealtimeInspectionRequest(
        Long targetId,
        Long cameraId,
        Long thresholdId
) {
}
