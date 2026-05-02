package com.example.factoryguard.application.dto.operation;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class SystemComponentStatusResult {

    private final Long componentStatusId;
    private final String componentType;
    private final String componentName;
    private final String status;
    private final String message;
    private final BigDecimal cpuUsage;
    private final BigDecimal memoryUsage;
    private final BigDecimal diskUsage;
    private final String hostName;
    private final String instanceId;
    private final Integer responseTimeMs;
    private final LocalDateTime checkedAt;
    private final LocalDateTime createdAt;
}
