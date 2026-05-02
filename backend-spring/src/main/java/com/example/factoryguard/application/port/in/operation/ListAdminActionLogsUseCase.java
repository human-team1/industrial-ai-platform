package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.AdminActionLogResult;
import com.example.factoryguard.application.dto.operation.ListAuditLogsQuery;
import com.example.factoryguard.application.dto.operation.OperationPageResponse;

public interface ListAdminActionLogsUseCase {

    OperationPageResponse<AdminActionLogResult> executeAdminActions(ListAuditLogsQuery query);
}
