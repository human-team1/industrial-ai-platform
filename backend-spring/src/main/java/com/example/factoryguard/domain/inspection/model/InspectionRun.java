package com.example.factoryguard.domain.inspection.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class InspectionRun {

    private final Long inspectionId;
    private final Long organizationId;
    private final Long userId;
    private final Long targetId;
    private final RunType runType;
    private final String inputType;
    private final String sourceType;
    private final String sourceId;
    private final RunStatus runStatus;
    private final double appliedThreshold;
    private final String idempotencyKey;
    private final String payloadFingerprint;
    private final String errorCode;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
}
