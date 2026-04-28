package com.example.factoryguard.application.dto.operation;

import com.example.factoryguard.domain.operation.vo.AuditActionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditLogResult {

    private final Long auditLogId;
    private final Long actorUserId;
    private final AuditActionType actionType;
    private final String targetType;
    private final Long targetId;
    private final LocalDateTime createdAt;
}
