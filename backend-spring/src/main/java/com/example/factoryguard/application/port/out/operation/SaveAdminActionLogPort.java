package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.AdminActionLog;

public interface SaveAdminActionLogPort {

    AdminActionLog save(AdminActionLog adminActionLog);
}
