package com.example.factoryguard.domain.inspection.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InspectionResult {

    private final Long resultId;
    private final Long inspectionId;
    private final double score;
    private final double confidence;
    private final DecisionCode decisionCode;
    private final DecisionCode finalDecisionCode;
    private final String resultStatus;
    private final String thresholdSource;
    private final Long thresholdId;
    private final String thresholdVersion;
    private final Long modelVersionId;
    private final String failureReason;
    private final LocalDateTime createdAt;
}