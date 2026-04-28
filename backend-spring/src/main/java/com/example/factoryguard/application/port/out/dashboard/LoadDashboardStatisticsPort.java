package com.example.factoryguard.application.port.out.dashboard;

import com.example.factoryguard.application.dto.dashboard.*;

import java.util.List;

public interface LoadDashboardStatisticsPort {

    DashboardSummaryResult loadSummary(Long organizationId);

    List<InspectionTrendResult> loadInspectionTrend(Long organizationId);

    List<DecisionRatioResult> loadDecisionRatio(Long organizationId);

    List<TopDefectTypeResult> loadTopDefectTypes(Long organizationId);

    List<EquipmentScoreTrendResult> loadEquipmentScoreTrend(Long organizationId, Long targetId);

    List<RecentInspectionResult> loadRecentInspectionResults(Long organizationId, int limit);
}
