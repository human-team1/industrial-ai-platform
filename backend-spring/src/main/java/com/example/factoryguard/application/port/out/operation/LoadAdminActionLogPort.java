package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.AdminActionLog;

import java.util.List;

public interface LoadAdminActionLogPort {

    List<AdminActionLog> findAll(Long actorUserId, String actionType, String targetType);
}
