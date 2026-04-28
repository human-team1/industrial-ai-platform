package com.example.factoryguard.application.dto.inspection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListInspectionRunsQuery {

    private final Long userId;
    private final Long organizationId;
    private final String runStatus;
    private final int page;
    private final int size;
}
