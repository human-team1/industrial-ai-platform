package com.example.factoryguard.application.dto.operation;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecordAuditLogCommand {

    private final Long actorUserId;
    private final String actionType;
    private final String targetType;
    private final Long targetId;
    private final String beforeJson;
    private final String afterJson;
}
