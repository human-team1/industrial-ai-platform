package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.AuditLog;

import java.util.List;

public interface LoadAuditLogPort {

    List<AuditLog> findAll(Long actorUserId, String actionType, String targetType);
}
