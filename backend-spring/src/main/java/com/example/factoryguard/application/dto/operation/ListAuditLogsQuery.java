package com.example.factoryguard.application.dto.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListAuditLogsQuery {

    private final Long actorUserId;
    private final String actionType;
    private final String targetType;
    private final int page;
    private final int size;
}
