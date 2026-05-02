package com.example.factoryguard.application.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DashboardSystemSnapshot(
        BigDecimal cpuUsage,
        BigDecimal memoryUsage,
        BigDecimal diskUsage,
        Integer responseTimeMs,
        LocalDateTime createdAt
) {
}
