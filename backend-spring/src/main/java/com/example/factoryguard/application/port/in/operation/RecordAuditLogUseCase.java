package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.RecordAuditLogCommand;

public interface RecordAuditLogUseCase {

    void recordAuditLog(RecordAuditLogCommand command);
}
