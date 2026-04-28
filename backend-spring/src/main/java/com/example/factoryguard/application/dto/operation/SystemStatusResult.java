package com.example.factoryguard.application.dto.operation;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SystemStatusResult {

    private final BigDecimal cpuUsage;
    private final BigDecimal memoryUsage;
    private final BigDecimal diskUsage;
    private final Integer responseTimeMs;
    private final LocalDateTime snapshotAt;
}
