package com.example.factoryguard.application.dto.operation;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminActionLogResult {

    private final Long adminActionId;
    private final Long actorUserId;
    private final String actionType;
    private final String targetType;
    private final Long targetId;
    private final String reason;
    private final LocalDateTime createdAt;
}
