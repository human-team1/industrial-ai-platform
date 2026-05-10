package com.example.factoryguard.adapter.in.web.inspection.dto;

import com.example.factoryguard.domain.inspection.model.RunStatus;

import java.time.LocalDateTime;

public record StopInspectionResponse(
        Long inspectionId,
        RunStatus runStatus,
        LocalDateTime completedAt
) {
}
