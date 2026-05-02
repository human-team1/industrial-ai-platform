package com.example.factoryguard.adapter.in.web.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateOperationPolicyRequest {

    private String policyValue;
    private Boolean isActive;
}
