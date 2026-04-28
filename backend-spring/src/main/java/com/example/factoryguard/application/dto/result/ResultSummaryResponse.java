package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ResultSummaryResponse {

    private final Long resultId;
    private final Long inspectionId;
    private final Long targetId;
    private final String targetName;
    private final String equipmentName;
    private final String productName;
    private final String runType;
    private final String inputType;
    private final BigDecimal score;
    private final BigDecimal confidence;
    private final String decisionCode;
    private final String finalDecisionCode;
    private final String resultStatus;
    private final Boolean reviewRequired;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime createdAt;
}
