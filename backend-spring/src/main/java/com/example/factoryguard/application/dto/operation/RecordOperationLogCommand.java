package com.example.factoryguard.application.dto.operation;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecordOperationLogCommand {

    private final String eventType;
    private final String eventStatus;
    private final String logLevel;
    private final String sourceComponent;
    private final Long actorUserId;
    private final String detailMessage;
    private final String relatedPath;
}
