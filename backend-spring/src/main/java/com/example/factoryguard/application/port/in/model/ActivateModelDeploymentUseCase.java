package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ActivateModelDeploymentCommand;
import com.example.factoryguard.application.dto.model.ModelDeploymentResponse;

public interface ActivateModelDeploymentUseCase {
    ModelDeploymentResponse activateModelDeployment(ActivateModelDeploymentCommand command);
}
