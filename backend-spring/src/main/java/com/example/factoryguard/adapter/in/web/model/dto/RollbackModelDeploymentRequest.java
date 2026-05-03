package com.example.factoryguard.adapter.in.web.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RollbackModelDeploymentRequest {

    private Long rollbackToDeploymentId;
    private String reason;
}
