package com.example.factoryguard.adapter.out.persistence.dashboard;

import com.example.factoryguard.application.dto.dashboard.DashboardDecisionCounts;
import com.example.factoryguard.application.dto.dashboard.DashboardOverviewResult;
import com.example.factoryguard.application.dto.dashboard.DashboardSummaryMetrics;
import com.example.factoryguard.application.dto.dashboard.DashboardSystemSnapshot;
import com.example.factoryguard.application.port.out.dashboard.LoadDashboardOverviewPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DashboardOverviewPersistenceAdapter implements LoadDashboardOverviewPort {

    private final DashboardOverviewQueryRepository repository;

    @Override
    public DashboardDecisionCounts loadDecisionCounts(LocalDateTime from, LocalDateTime to, Long organizationId) {
        return repository.loadDecisionCounts(from, to, organizationId);
    }

    @Override
    public List<DashboardOverviewResult.TrendPoint> loadTrend(LocalDateTime from, LocalDateTime to, Long organizationId) {
        return repository.loadTrend(from, to, organizationId);
    }

    @Override
    public List<DashboardOverviewResult.TopEquipmentAnomalyRate> loadTopEquipmentAnomalyRates(
            LocalDateTime from,
            LocalDateTime to,
            Long organizationId,
            int limit
    ) {
        return repository.loadTopEquipmentAnomalyRates(from, to, organizationId, limit);
    }

    @Override
    public List<DashboardOverviewResult.RecentResult> loadRecentResults(Long organizationId, int limit) {
        return repository.loadRecentResults(organizationId, limit);
    }

    @Override
    public List<DashboardOverviewResult.RecentNotification> loadRecentNotifications(Long organizationId, int limit) {
        return repository.loadRecentNotifications(organizationId, limit);
    }

    @Override
    public DashboardSummaryMetrics loadSummary(Long organizationId) {
        return repository.loadSummary(organizationId);
    }

    @Override
    public Optional<DashboardSystemSnapshot> loadLatestSystemSnapshot() {
        return repository.loadLatestSystemSnapshot();
    }
}
