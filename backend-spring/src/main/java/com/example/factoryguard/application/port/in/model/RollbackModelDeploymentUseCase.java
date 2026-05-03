package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ModelDeploymentResponse;
import com.example.factoryguard.application.dto.model.RollbackModelDeploymentCommand;

public interface RollbackModelDeploymentUseCase {

    ModelDeploymentResponse rollbackModelDeployment(RollbackModelDeploymentCommand command);
}
