package com.example.factoryguard.application.dto.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListAsyncJobsQuery {

    private final String jobStatus;
    private final String jobType;
    private final int page;
    private final int size;
}
