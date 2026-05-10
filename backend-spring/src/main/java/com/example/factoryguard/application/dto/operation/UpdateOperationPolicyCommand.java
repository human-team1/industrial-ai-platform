package com.example.factoryguard.application.dto.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateOperationPolicyCommand {

    private final Long adminUserId;
    private final Long policyId;
    private final String policyValue;
    private final Boolean isActive;
}
