package com.example.factoryguard.application.dto.model;

import com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelVersionJpaEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResolvedModelDeployment {

    private final ModelDeploymentJpaEntity deployment;
    private final ModelVersionJpaEntity version;
}
