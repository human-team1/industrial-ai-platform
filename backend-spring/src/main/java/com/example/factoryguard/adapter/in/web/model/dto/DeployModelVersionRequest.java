package com.example.factoryguard.adapter.in.web.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployModelVersionRequest {

    private Long organizationId;
    private Long targetId;
    private String deploymentScope;
    private String reason;
}
