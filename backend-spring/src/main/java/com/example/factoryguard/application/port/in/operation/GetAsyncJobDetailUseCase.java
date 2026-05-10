package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.AsyncJobDetail;

public interface GetAsyncJobDetailUseCase {

    AsyncJobDetail execute(Long jobId);
}
