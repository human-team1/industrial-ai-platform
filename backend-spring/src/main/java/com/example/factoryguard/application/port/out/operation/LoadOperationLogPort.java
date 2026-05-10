package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.OperationLog;

import java.util.List;

public interface LoadOperationLogPort {

    List<OperationLog> findAll(String eventType, String eventStatus);
}
