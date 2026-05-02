package com.example.factoryguard.application.dto.dashboard;

import java.time.LocalDate;

public record DashboardSummaryMetrics(
        long registeredTargetCount,
        long activeModelCount,
        long totalDataSizeBytes,
        LocalDate latestTrainingDate
) {
}
