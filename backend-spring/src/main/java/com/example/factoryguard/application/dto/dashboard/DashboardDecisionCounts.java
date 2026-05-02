package com.example.factoryguard.application.dto.dashboard;

public record DashboardDecisionCounts(
        long totalInspectionCount,
        long anomalyCount,
        long normalCount,
        long recheckCount
) {
}
