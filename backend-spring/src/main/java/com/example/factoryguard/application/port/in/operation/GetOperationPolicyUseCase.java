package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.ListOperationPoliciesQuery;
import com.example.factoryguard.application.dto.operation.OperationPolicyResult;

import java.util.List;

public interface GetOperationPolicyUseCase {

    List<OperationPolicyResult> execute(ListOperationPoliciesQuery query);
}
