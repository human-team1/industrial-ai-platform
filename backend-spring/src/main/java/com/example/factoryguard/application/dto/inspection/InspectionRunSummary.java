package com.example.factoryguard.application.dto.inspection;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InspectionRunSummary {

    private final Long inspectionId;
    private final Long targetId;
    private final String runType;
    private final String runStatus;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
}
