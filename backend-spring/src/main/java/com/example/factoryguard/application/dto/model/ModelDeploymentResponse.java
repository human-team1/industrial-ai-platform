package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ModelDeploymentResponse {

    private final Long deploymentId;
    private final Long organizationId;
    private final Long targetId;
    private final Long modelVersionId;
    private final Long modelId;
    private final String modelName;
    private final String versionName;
    private final String deploymentScope;
    private final String deployStatus;
    private final Boolean isActive;
    private final LocalDateTime deployedAt;
    private final Long deployedBy;
    private final Long rollbackFromDeploymentId;
    private final String reason;
}
