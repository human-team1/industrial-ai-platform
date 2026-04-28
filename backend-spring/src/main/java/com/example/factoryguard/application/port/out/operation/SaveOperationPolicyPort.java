package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.OperationPolicy;

public interface SaveOperationPolicyPort {

    OperationPolicy save(OperationPolicy operationPolicy);
}
