package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.AuditLogResult;
import com.example.factoryguard.application.dto.operation.ListAuditLogsQuery;

import java.util.List;

public interface ListAuditLogsUseCase {

    List<AuditLogResult> execute(ListAuditLogsQuery query);
}
