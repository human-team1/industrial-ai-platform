package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.AuditLog;

public interface SaveAuditLogPort {

    AuditLog save(AuditLog auditLog);
}
