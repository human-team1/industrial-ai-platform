package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.AsyncJobSummary;
import com.example.factoryguard.application.dto.operation.ListAsyncJobsQuery;
import com.example.factoryguard.application.dto.operation.OperationPageResponse;

public interface ListAsyncJobsUseCase {

    OperationPageResponse<AsyncJobSummary> execute(ListAsyncJobsQuery query);
}
