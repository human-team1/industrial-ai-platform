package com.example.factoryguard.application.dto.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListOperationLogsQuery {

    private final String eventType;
    private final String eventStatus;
    private final int page;
    private final int size;
}
