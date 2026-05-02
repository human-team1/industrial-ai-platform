package com.example.factoryguard.application.service.dashboard;

import com.example.factoryguard.application.dto.dashboard.DashboardDecisionCounts;
import com.example.factoryguard.application.dto.dashboard.DashboardOverviewQuery;
import com.example.factoryguard.application.dto.dashboard.DashboardOverviewResult;
import com.example.factoryguard.application.dto.dashboard.DashboardSummaryMetrics;
import com.example.factoryguard.application.dto.dashboard.DashboardSystemSnapshot;
import com.example.factoryguard.application.port.in.dashboard.GetDashboardOverviewUseCase;
import com.example.factoryguard.application.port.out.dashboard.LoadDashboardOverviewPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardOverviewService implements GetDashboardOverviewUseCase {

    private static final int RECENT_RESULT_LIMIT = 5;
    private static final int RECENT_NOTIFICATION_LIMIT = 3;
    private static final int TOP_EQUIPMENT_LIMIT = 5;

    private final LoadDashboardOverviewPort loadDashboardOverviewPort;
    private final SecurityUtils securityUtils;

    @Override
    public DashboardOverviewResult execute(DashboardOverviewQuery query) {
        LocalDate endDate = query.endDate() != null ? query.endDate() : LocalDate.now();
        LocalDate startDate = query.startDate() != null ? query.startDate() : endDate.minusDays(6);
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "startDate는 endDate보다 늦을 수 없습니다.");
        }

        Long scopedOrganizationId = resolveOrganizationScope(query.organizationId());
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.plusDays(1).atStartOfDay();

        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDateTime previousFrom = from.minusDays(periodDays);
        LocalDateTime previousTo = from;

        DashboardDecisionCounts current = loadDashboardOverviewPort.loadDecisionCounts(from, to, scopedOrganizationId);
        DashboardDecisionCounts previous = loadDashboardOverviewPort.loadDecisionCounts(previousFrom, previousTo, scopedOrganizationId);
        DashboardSummaryMetrics summary = loadDashboardOverviewPort.loadSummary(scopedOrganizationId);

        return new DashboardOverviewResult(
                new DashboardOverviewResult.Period(startDate, endDate),
                buildKpis(current, previous),
                loadDashboardOverviewPort.loadTrend(from, to, scopedOrganizationId),
                loadDashboardOverviewPort.loadTopEquipmentAnomalyRates(from, to, scopedOrganizationId, TOP_EQUIPMENT_LIMIT),
                loadDashboardOverviewPort.loadRecentResults(scopedOrganizationId, RECENT_RESULT_LIMIT),
                loadDashboardOverviewPort.loadRecentNotifications(scopedOrganizationId, RECENT_NOTIFICATION_LIMIT),
                new DashboardOverviewResult.Summary(
                        summary.registeredTargetCount(),
                        summary.activeModelCount(),
                        summary.totalDataSizeBytes(),
                        summary.latestTrainingDate()
                ),
                buildSystemStatus(loadDashboardOverviewPort.loadLatestSystemSnapshot().orElse(null))
        );
    }

    private Long resolveOrganizationScope(Long requestedOrganizationId) {
        if (securityUtils.isSiteAdmin()) {
            return requestedOrganizationId;
        }
        return securityUtils.requireOrganizationId();
    }

    private DashboardOverviewResult.Kpis buildKpis(DashboardDecisionCounts current, DashboardDecisionCounts previous) {
        double currentAnomalyRate = rate(current.anomalyCount(), current.totalInspectionCount());
        double previousAnomalyRate = rate(previous.anomalyCount(), previous.totalInspectionCount());
        return new DashboardOverviewResult.Kpis(
                current.totalInspectionCount(),
                changeRate(current.totalInspectionCount(), previous.totalInspectionCount()),
                current.anomalyCount(),
                changeRate(current.anomalyCount(), previous.anomalyCount()),
                current.normalCount(),
                changeRate(current.normalCount(), previous.normalCount()),
                currentAnomalyRate,
                round(currentAnomalyRate - previousAnomalyRate)
        );
    }

    private DashboardOverviewResult.SystemStatus buildSystemStatus(DashboardSystemSnapshot snapshot) {
        if (snapshot == null) {
            LocalDateTime now = LocalDateTime.now();
            return new DashboardOverviewResult.SystemStatus("NORMAL", "NORMAL", "NORMAL", "NORMAL", now);
        }

        String storageStatus = statusFromUsage(snapshot.diskUsage());
        String overallStatus = "ERROR".equals(storageStatus) ? "ERROR" : ("WARNING".equals(storageStatus) ? "WARNING" : "NORMAL");
        // MVP fallback: component-specific health table is not present, so DB snapshot based storage health is reused.
        return new DashboardOverviewResult.SystemStatus(
                overallStatus,
                "NORMAL",
                "NORMAL",
                storageStatus,
                snapshot.createdAt()
        );
    }

    private String statusFromUsage(BigDecimal usage) {
        if (usage == null) {
            return "NORMAL";
        }
        if (usage.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "ERROR";
        }
        if (usage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "WARNING";
        }
        return "NORMAL";
    }

    private double changeRate(long current, long previous) {
        if (previous == 0) {
            return 0.0;
        }
        return round(((double) current - previous) / previous * 100.0);
    }

    private double rate(long value, long total) {
        if (total == 0) {
            return 0.0;
        }
        return round((double) value / total * 100.0);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
