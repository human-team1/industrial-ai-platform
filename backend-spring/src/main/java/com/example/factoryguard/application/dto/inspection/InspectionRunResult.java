package com.example.factoryguard.application.dto.inspection;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InspectionRunResult {

    private final Long inspectionId;
    private final Long organizationId;
    private final Long userId;
    private final Long targetId;
    private final String runType;
    private final String inputType;
    private final String sourceType;
    private final String runStatus;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
}
