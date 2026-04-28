package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.AsyncJobSummary;

import java.util.List;

public interface ListAsyncJobsUseCase {

    List<AsyncJobSummary> execute(String jobStatus);
}
