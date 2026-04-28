package com.example.factoryguard.application.dto.operation;

import com.example.factoryguard.domain.operation.vo.OperationEventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OperationLogResult {

    private final Long operationLogId;
    private final String eventType;
    private final OperationEventStatus eventStatus;
    private final String detailMessage;
    private final String relatedPath;
    private final LocalDateTime createdAt;
}
