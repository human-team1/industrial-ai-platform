package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeactivateModelDeploymentCommand {

    private final Long deploymentId;
    private final String reason;
}
