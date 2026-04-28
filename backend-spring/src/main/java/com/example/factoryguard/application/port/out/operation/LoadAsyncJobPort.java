package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.AsyncJob;

import java.util.List;
import java.util.Optional;

public interface LoadAsyncJobPort {

    Optional<AsyncJob> findById(Long jobId);

    List<AsyncJob> findAllByStatus(String jobStatus);
}
