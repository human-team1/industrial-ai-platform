package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListModelDeploymentsQuery {

    private final Long organizationId;
    private final Long targetId;
    private final Long modelVersionId;
    private final String deploymentScope;
    private final Boolean isActive;
    private final int page;
    private final int size;
    private final String sort;
}
