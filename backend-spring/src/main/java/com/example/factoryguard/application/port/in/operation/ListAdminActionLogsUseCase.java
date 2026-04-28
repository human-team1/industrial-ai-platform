package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.AdminActionLogResult;
import com.example.factoryguard.application.dto.operation.ListAuditLogsQuery;

import java.util.List;

public interface ListAdminActionLogsUseCase {

    List<AdminActionLogResult> execute(ListAuditLogsQuery query);
}
