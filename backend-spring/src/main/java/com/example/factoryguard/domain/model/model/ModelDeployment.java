package com.example.factoryguard.domain.model.model;

import com.example.factoryguard.domain.model.vo.ModelDeployStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ModelDeployment {

    private final Long deploymentId;
    private final Long modelVersionId;
    private final LocalDateTime deployedAt;
    private final Boolean rollbackFlag;
    private final ModelDeployStatus deployStatus;
}
