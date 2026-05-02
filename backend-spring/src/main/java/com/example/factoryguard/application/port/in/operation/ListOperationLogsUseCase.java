package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.ListOperationLogsQuery;
import com.example.factoryguard.application.dto.operation.OperationLogResult;
import com.example.factoryguard.application.dto.operation.OperationPageResponse;

public interface ListOperationLogsUseCase {

    OperationPageResponse<OperationLogResult> execute(ListOperationLogsQuery query);
}
