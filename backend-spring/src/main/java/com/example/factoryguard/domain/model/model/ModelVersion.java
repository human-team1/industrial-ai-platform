package com.example.factoryguard.domain.model.model;

import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelDeployStatus;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ModelVersion {

    private final Long modelVersionId;
    private final Long modelId;
    private final Long fileId;
    private final String versionName;
    private final ModelCategory modelCategory;
    private final ModelProfile modelProfile;
    private final String framework;
    private final String inputSize;
    private final BigDecimal thresholdDefault;
    private final BigDecimal accuracy;
    private final BigDecimal precisionScore;
    private final BigDecimal recallScore;
    private final BigDecimal f1Score;
    private final BigDecimal aurocScore;
    private final ModelDeployStatus deployStatus;
    private final Boolean isActive;
    private final LocalDateTime validatedAt;
    private final Long validatedBy;
    private final LocalDateTime createdAt;
}
