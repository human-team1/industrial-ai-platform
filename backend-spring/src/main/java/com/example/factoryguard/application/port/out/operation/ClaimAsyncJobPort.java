package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.AsyncJob;
import com.example.factoryguard.domain.operation.vo.AsyncJobType;

import java.util.Optional;

public interface ClaimAsyncJobPort {

    Optional<AsyncJob> claimNextPending(AsyncJobType jobType);
}
