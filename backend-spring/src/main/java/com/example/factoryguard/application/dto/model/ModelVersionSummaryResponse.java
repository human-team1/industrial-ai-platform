package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ModelVersionSummaryResponse {

    private final Long modelVersionId;
    private final Long modelId;
    private final String modelName;
    private final String versionName;
    private final String modelCategory;
    private final String modelProfile;
    private final String framework;
    private final String inputSize;
    private final BigDecimal thresholdDefault;
    private final BigDecimal accuracy;
    private final BigDecimal precisionScore;
    private final BigDecimal recallScore;
    private final BigDecimal f1Score;
    private final BigDecimal aurocScore;
    private final String deployStatus;
    private final Boolean isActive;
    private final LocalDateTime validatedAt;
    private final Long validatedBy;
    private final LocalDateTime createdAt;
}
