package com.example.factoryguard.adapter.in.web.inspection.dto;

import com.example.factoryguard.domain.inspection.model.RunStatus;

import java.time.LocalDateTime;

public record RealtimeInspectionResponse(
        Long inspectionId,
        RunStatus runStatus,
        Long cameraId,
        LocalDateTime startedAt
) {
}
