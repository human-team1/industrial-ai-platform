package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.DeactivateModelDeploymentCommand;
import com.example.factoryguard.application.dto.model.ModelDeploymentResponse;

public interface DeleteModelDeploymentUseCase {
    ModelDeploymentResponse deleteModelDeployment(DeactivateModelDeploymentCommand command);
}
