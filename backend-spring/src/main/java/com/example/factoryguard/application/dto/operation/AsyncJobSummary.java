package com.example.factoryguard.application.dto.operation;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AsyncJobSummary {

    private final Long jobId;
    private final String jobType;
    private final String jobStatus;
    private final String targetType;
    private final Long targetId;
    private final String errorMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime completedAt;
}
