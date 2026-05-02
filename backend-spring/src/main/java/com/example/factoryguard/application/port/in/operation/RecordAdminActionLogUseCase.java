package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.RecordAdminActionLogCommand;

public interface RecordAdminActionLogUseCase {

    void recordAdminActionLog(RecordAdminActionLogCommand command);
}
