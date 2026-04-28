package com.example.factoryguard.application.dto.operation;

import com.example.factoryguard.domain.operation.vo.OperationPolicyType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OperationPolicyResult {

    private final Long operationPolicyId;
    private final OperationPolicyType policyType;
    private final String policyValue;
    private final Long updatedBy;
}
