package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResultDecisionResponse {

    private final Double score;
    private final Double confidence;
    private final String decisionCode;
    private final String finalDecisionCode;
    private final String resultStatus;
    private final String thresholdSource;
    private final Double appliedThreshold;
    private final Long modelVersionId;
    private final String failureReason;
    private final LocalDateTime createdAt;
}
