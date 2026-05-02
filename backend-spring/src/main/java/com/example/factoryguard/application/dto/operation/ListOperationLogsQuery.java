package com.example.factoryguard.application.dto.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListOperationLogsQuery {

    private final String level;
    private final String sourceComponent;
    private final String eventType;
    private final String eventStatus;
    private final String startDate;
    private final String endDate;
    private final String sort;
    private final int page;
    private final int size;
}
