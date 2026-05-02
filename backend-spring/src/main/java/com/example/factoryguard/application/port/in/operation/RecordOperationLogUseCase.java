package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.RecordOperationLogCommand;

public interface RecordOperationLogUseCase {

    void recordOperationLog(RecordOperationLogCommand command);
}
