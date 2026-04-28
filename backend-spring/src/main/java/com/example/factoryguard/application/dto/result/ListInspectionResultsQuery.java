package com.example.factoryguard.application.dto.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListInspectionResultsQuery {

    private final Long organizationId;
    private final Long userId;
    private final String decisionCode;
    private final String resultStatus;
    private final int page;
    private final int size;
}
