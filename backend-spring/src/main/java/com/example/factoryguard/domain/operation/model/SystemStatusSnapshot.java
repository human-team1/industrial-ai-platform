package com.example.factoryguard.domain.operation.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SystemStatusSnapshot {

    private final Long snapshotId;
    private final BigDecimal cpuUsage;
    private final BigDecimal memoryUsage;
    private final BigDecimal diskUsage;
    private final Integer responseTimeMs;
    private final LocalDateTime createdAt;
}
