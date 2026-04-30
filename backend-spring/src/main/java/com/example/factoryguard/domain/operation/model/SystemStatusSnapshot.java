package com.example.factoryguard.domain.operation.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SystemStatusSnapshot {

    private final Long snapshotId;
    private final Double cpuUsage;
    private final Double memoryUsage;
    private final Double diskUsage;
    private final Integer responseTimeMs;
    private final LocalDateTime createdAt;
}
