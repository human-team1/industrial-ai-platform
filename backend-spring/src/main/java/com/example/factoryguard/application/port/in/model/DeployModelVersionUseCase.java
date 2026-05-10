package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.DeployModelVersionCommand;
import com.example.factoryguard.application.dto.model.ModelDeploymentResponse;

public interface DeployModelVersionUseCase {

    ModelDeploymentResponse deployModelVersion(DeployModelVersionCommand command);
}
