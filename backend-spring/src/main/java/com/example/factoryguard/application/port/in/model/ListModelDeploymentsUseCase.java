package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ListModelDeploymentsQuery;
import com.example.factoryguard.application.dto.model.ModelDeploymentResponse;
import com.example.factoryguard.application.dto.model.ModelPageResponse;

public interface ListModelDeploymentsUseCase {

    ModelPageResponse<ModelDeploymentResponse> listModelDeployments(ListModelDeploymentsQuery query);
}
