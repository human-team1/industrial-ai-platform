package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.AuditLogResult;
import com.example.factoryguard.application.dto.operation.ListAuditLogsQuery;
import com.example.factoryguard.application.dto.operation.OperationPageResponse;

public interface ListAuditLogsUseCase {

    OperationPageResponse<AuditLogResult> execute(ListAuditLogsQuery query);
}
