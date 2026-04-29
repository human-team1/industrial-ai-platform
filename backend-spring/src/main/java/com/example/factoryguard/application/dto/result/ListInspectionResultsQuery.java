package com.example.factoryguard.application.dto.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ListInspectionResultsQuery {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final String keyword;
    private final String equipmentName;
    private final String productName;
    private final String runType;
    private final String decision;
    private final String resultStatus;
    private final int page;
    private final int size;
}
