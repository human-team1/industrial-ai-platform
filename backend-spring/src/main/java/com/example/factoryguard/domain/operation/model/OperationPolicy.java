package com.example.factoryguard.domain.operation.model;

import com.example.factoryguard.domain.operation.vo.OperationPolicyType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OperationPolicy {

    private final Long operationPolicyId;
    private final OperationPolicyType policyType;
    private final String policyValue;
    private final LocalDateTime updatedAt;
    private final Long updatedBy;
}
