package com.example.factoryguard.application.port.out.dashboard;

import com.example.factoryguard.application.dto.dashboard.DashboardDecisionCounts;
import com.example.factoryguard.application.dto.dashboard.DashboardOverviewResult;
import com.example.factoryguard.application.dto.dashboard.DashboardSummaryMetrics;
import com.example.factoryguard.application.dto.dashboard.DashboardSystemSnapshot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoadDashboardOverviewPort {

    DashboardDecisionCounts loadDecisionCounts(LocalDateTime from, LocalDateTime to, Long organizationId);

    List<DashboardOverviewResult.TrendPoint> loadTrend(LocalDateTime from, LocalDateTime to, Long organizationId);

    List<DashboardOverviewResult.TopEquipmentAnomalyRate> loadTopEquipmentAnomalyRates(
            LocalDateTime from,
            LocalDateTime to,
            Long organizationId,
            int limit
    );

    List<DashboardOverviewResult.RecentResult> loadRecentResults(Long organizationId, int limit);

    List<DashboardOverviewResult.RecentNotification> loadRecentNotifications(Long organizationId, int limit);

    DashboardSummaryMetrics loadSummary(Long organizationId);

    Optional<DashboardSystemSnapshot> loadLatestSystemSnapshot();
}
