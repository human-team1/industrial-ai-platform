package com.example.factoryguard.application.dto.operation;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OperationPolicyResult {

    private final Long operationPolicyId;
    private final String policyCategory;
    private final String policyKey;
    private final String policyName;
    private final String policyValue;
    private final String valueType;
    private final String description;
    private final Boolean isActive;
    private final LocalDateTime updatedAt;
    private final Long updatedBy;
}
