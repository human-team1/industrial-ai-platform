package com.example.factoryguard.application.dto.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardOverviewResult(
        Period period,
        Kpis kpis,
        List<TrendPoint> trend,
        List<TopEquipmentAnomalyRate> topEquipmentAnomalyRates,
        List<RecentResult> recentResults,
        List<RecentNotification> recentNotifications,
        Summary summary,
        SystemStatus systemStatus
) {
    public record Period(LocalDate startDate, LocalDate endDate) {
    }

    public record Kpis(
            long totalInspectionCount,
            double totalInspectionChangeRate,
            long anomalyCount,
            double anomalyChangeRate,
            long normalCount,
            double normalChangeRate,
            double anomalyRate,
            double anomalyRateChangePoint
    ) {
    }

    public record TrendPoint(
            LocalDate date,
            long normalCount,
            long anomalyCount,
            long recheckCount,
            double anomalyRate
    ) {
    }

    public record TopEquipmentAnomalyRate(
            Long targetId,
            String equipmentName,
            long inspectionCount,
            long anomalyCount,
            double anomalyRate
    ) {
    }

    public record RecentResult(
            Long resultId,
            Long inspectionId,
            LocalDateTime inspectedAt,
            String equipmentName,
            String inspectionType,
            String decision,
            String decisionLabel,
            Double anomalyScore,
            String locationName
    ) {
    }

    public record RecentNotification(
            Long notificationId,
            String severity,
            String title,
            LocalDateTime createdAt,
            String targetUrl
    ) {
    }

    public record Summary(
            long registeredTargetCount,
            long activeModelCount,
            long totalDataSizeBytes,
            LocalDate latestTrainingDate
    ) {
    }

    public record SystemStatus(
            String overallStatus,
            String modelServerStatus,
            String streamServerStatus,
            String storageStatus,
            LocalDateTime lastUpdatedAt
    ) {
    }
}
