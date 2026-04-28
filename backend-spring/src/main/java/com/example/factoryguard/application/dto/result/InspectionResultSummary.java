package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InspectionResultSummary {

    private final Long resultId;
    private final Long inspectionId;
    private final double score;
    private final double confidence;
    private final String decisionCode;
    private final String finalDecisionCode;
    private final String resultStatus;
    private final LocalDateTime createdAt;
}
