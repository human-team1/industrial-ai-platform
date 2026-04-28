package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.OperationPolicy;
import com.example.factoryguard.domain.operation.vo.OperationPolicyType;

import java.util.Optional;

public interface LoadOperationPolicyPort {

    Optional<OperationPolicy> findByPolicyType(OperationPolicyType policyType);
}
