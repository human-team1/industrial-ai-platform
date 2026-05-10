package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.OperationPolicyResult;
import com.example.factoryguard.application.dto.operation.UpdateOperationPolicyCommand;

public interface UpdateOperationPolicyUseCase {

    OperationPolicyResult execute(UpdateOperationPolicyCommand command);
}
