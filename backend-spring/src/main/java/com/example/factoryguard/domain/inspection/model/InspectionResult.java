package com.example.factoryguard.domain.inspection.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class InspectionResult {

    private final Long resultId;
    private final Long inspectionId;
    private final BigDecimal score;
    private final BigDecimal confidence;
    private final DecisionCode decisionCode;
    private final DecisionCode finalDecisionCode;
    private final String resultStatus;
    private final String thresholdSource;
    private final Long thresholdId;
    private final Integer thresholdVersion;
    private final Long modelVersionId;
    private final String failureReason;
    private final LocalDateTime createdAt;
}