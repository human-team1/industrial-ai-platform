package com.example.factoryguard.domain.model.model;

import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.DeploymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ModelDeployment {

    private final Long deploymentId;
    private final Long organizationId;
    private final Long targetId;
    private final Long modelVersionId;
    private final DeploymentScope deploymentScope;
    private final DeploymentStatus deployStatus;
    private final Boolean isActive;
    private final LocalDateTime deployedAt;
    private final Long deployedBy;
    private final Long rollbackFromDeploymentId;
    private final String reason;
    private final Boolean rollbackFlag;
}
