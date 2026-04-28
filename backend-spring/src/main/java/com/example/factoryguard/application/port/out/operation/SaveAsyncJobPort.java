package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.AsyncJob;

public interface SaveAsyncJobPort {

    AsyncJob save(AsyncJob asyncJob);
}
