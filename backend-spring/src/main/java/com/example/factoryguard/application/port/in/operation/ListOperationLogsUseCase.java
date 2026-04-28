package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.ListOperationLogsQuery;
import com.example.factoryguard.application.dto.operation.OperationLogResult;

import java.util.List;

public interface ListOperationLogsUseCase {

    List<OperationLogResult> execute(ListOperationLogsQuery query);
}
