package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ResultDecisionResponse {

    private final BigDecimal score;
    private final BigDecimal confidence;
    private final String decisionCode;
    private final String finalDecisionCode;
    private final String resultStatus;
    private final String thresholdSource;
    private final BigDecimal appliedThreshold;
    private final BigDecimal imageThreshold;
    private final Long modelVersionId;
    private final String failureReason;
    private final LocalDateTime createdAt;
}
