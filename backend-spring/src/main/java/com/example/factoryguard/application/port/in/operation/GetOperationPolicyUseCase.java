package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.OperationPolicyResult;
import com.example.factoryguard.domain.operation.vo.OperationPolicyType;

public interface GetOperationPolicyUseCase {

    OperationPolicyResult execute(OperationPolicyType policyType);
}
