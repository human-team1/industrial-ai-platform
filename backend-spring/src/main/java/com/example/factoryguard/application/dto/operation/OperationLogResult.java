package com.example.factoryguard.application.dto.operation;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OperationLogResult {

    private final Long operationLogId;
    private final String eventType;
    private final String eventStatus;
    private final String logLevel;
    private final String sourceComponent;
    private final String requestId;
    private final Long actorUserId;
    private final String detailMessage;
    private final String relatedPath;
    private final LocalDateTime createdAt;
}
