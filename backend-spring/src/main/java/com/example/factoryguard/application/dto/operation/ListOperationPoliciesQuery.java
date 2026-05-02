package com.example.factoryguard.application.dto.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListOperationPoliciesQuery {

    private final String category;
    private final boolean activeOnly;
}
