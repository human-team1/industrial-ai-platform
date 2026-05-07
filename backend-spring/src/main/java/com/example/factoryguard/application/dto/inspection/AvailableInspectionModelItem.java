package com.example.factoryguard.application.dto.inspection;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AvailableInspectionModelItem {

    private final Long deploymentId;
    private final Long modelVersionId;
    private final Long modelId;
    private final String modelName;
    private final String versionName;
    private final String displayName;
    private final String modelCategory;
    private final String modelProfile;
    private final String deploymentScope;
    private final Long organizationId;
    private final Long targetId;
    private final String modelVersionStatus;
    private final String deploymentStatus;
    private final Boolean isActive;
    private final BigDecimal thresholdDefault;
    private final LocalDateTime createdAt;
}
