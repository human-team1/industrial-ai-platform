package com.example.factoryguard.application.dto.operation;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SystemStatusResult {

    private final Double cpuUsage;
    private final Double memoryUsage;
    private final Double diskUsage;
    private final Integer responseTimeMs;
    private final LocalDateTime snapshotAt;
}
