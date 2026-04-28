package com.example.factoryguard.domain.operation.model;

import com.example.factoryguard.domain.operation.vo.AsyncJobStatus;
import com.example.factoryguard.domain.operation.vo.AsyncJobType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AsyncJob {

    private final Long jobId;
    private final AsyncJobType jobType;
    private final AsyncJobStatus jobStatus;
    private final String targetType;
    private final Long targetId;
    private final String errorMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime completedAt;
}
