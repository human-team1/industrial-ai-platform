package com.example.factoryguard.application.dto.operation;

import com.example.factoryguard.domain.operation.vo.OperationPolicyType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateOperationPolicyCommand {

    private final Long adminUserId;
    private final OperationPolicyType policyType;
    private final String policyValue;
}
