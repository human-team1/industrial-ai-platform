package com.example.factoryguard.adapter.in.web.inspection.dto;

import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.model.RunType;

import java.time.LocalDateTime;

public record InspectionRunResponse(
        Long inspectionId,
        Long organizationId,
        Long userId,
        Long targetId,
        RunType runType,
        RunStatus runStatus,
        double appliedThreshold,
        String errorCode,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {
}
