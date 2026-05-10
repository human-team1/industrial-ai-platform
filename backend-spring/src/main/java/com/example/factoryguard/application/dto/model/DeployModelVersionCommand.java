package com.example.factoryguard.application.dto.model;

import com.example.factoryguard.domain.model.vo.DeploymentScope;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeployModelVersionCommand {

    private final Long versionId;
    private final Long organizationId;
    private final Long targetId;
    private final DeploymentScope deploymentScope;
    private final String reason;
}
