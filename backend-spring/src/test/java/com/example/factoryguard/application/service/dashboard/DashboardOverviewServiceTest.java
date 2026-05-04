package com.example.factoryguard.application.service.dashboard;

import com.example.factoryguard.application.dto.dashboard.DashboardDecisionCounts;
import com.example.factoryguard.application.dto.dashboard.DashboardOverviewQuery;
import com.example.factoryguard.application.dto.dashboard.DashboardOverviewResult;
import com.example.factoryguard.application.dto.dashboard.DashboardSummaryMetrics;
import com.example.factoryguard.application.port.out.dashboard.LoadDashboardOverviewPort;
import com.example.factoryguard.config.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardOverviewServiceTest {

    @Mock private LoadDashboardOverviewPort loadDashboardOverviewPort;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private DashboardOverviewService service;

    @BeforeEach
    void stubSecurityAndDefaults() {
        lenient().when(securityUtils.isSiteAdmin()).thenReturn(false);
        lenient().when(securityUtils.requireOrganizationId()).thenReturn(100L);
        lenient().when(loadDashboardOverviewPort.loadTrend(any(), any(), any())).thenReturn(List.of());
        lenient().when(loadDashboardOverviewPort.loadTopEquipmentAnomalyRates(any(), any(), any(), anyInt())).thenReturn(List.of());
        lenient().when(loadDashboardOverviewPort.loadRecentResults(any(), anyInt())).thenReturn(List.of());
        lenient().when(loadDashboardOverviewPort.loadRecentNotifications(any(), anyInt())).thenReturn(List.of());
        lenient().when(loadDashboardOverviewPort.loadSummary(any())).thenReturn(
                new DashboardSummaryMetrics(0L, 0L, 0L, null));
        lenient().when(loadDashboardOverviewPort.loadLatestSystemSnapshot()).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("No.35 KPI 집계 - 정상/불량/재검사 건수 및 비율 계산")
    void computesKpiCountsAndRatios() {
        // current: total=100, anomaly=20, normal=70, recheck=10 → anomalyRate 20.0%
        // previous: total=80, anomaly=10, normal=60, recheck=10 → anomalyRate 12.5%
        when(loadDashboardOverviewPort.loadDecisionCounts(any(LocalDateTime.class), any(LocalDateTime.class), eq(100L)))
                .thenReturn(
                        new DashboardDecisionCounts(100, 20, 70, 10),
                        new DashboardDecisionCounts(80, 10, 60, 10)
                );

        DashboardOverviewResult result = service.execute(
                new DashboardOverviewQuery(LocalDate.of(2026, 4, 28), LocalDate.of(2026, 5, 4), null));

        DashboardOverviewResult.Kpis kpis = result.kpis();
        assertThat(kpis.totalInspectionCount()).isEqualTo(100);
        assertThat(kpis.anomalyCount()).isEqualTo(20);
        assertThat(kpis.normalCount()).isEqualTo(70);
        assertThat(kpis.anomalyRate()).isEqualTo(20.0);
        assertThat(kpis.totalInspectionChangeRate()).isEqualTo(25.0); // (100-80)/80 * 100
        assertThat(kpis.anomalyChangeRate()).isEqualTo(100.0);        // (20-10)/10 * 100
        assertThat(kpis.anomalyRateChangePoint()).isEqualTo(7.5);     // 20.0 - 12.5
    }

    @Test
    @DisplayName("No.35 이전 기간 0건 - 변화율 0으로 안전 처리")
    void zeroPreviousPeriodReturnsZeroChange() {
        when(loadDashboardOverviewPort.loadDecisionCounts(any(LocalDateTime.class), any(LocalDateTime.class), eq(100L)))
                .thenReturn(
                        new DashboardDecisionCounts(50, 5, 40, 5),
                        new DashboardDecisionCounts(0, 0, 0, 0)
                );

        DashboardOverviewResult result = service.execute(
                new DashboardOverviewQuery(LocalDate.of(2026, 4, 28), LocalDate.of(2026, 5, 4), null));

        assertThat(result.kpis().totalInspectionChangeRate()).isEqualTo(0.0);
        assertThat(result.kpis().anomalyChangeRate()).isEqualTo(0.0);
        assertThat(result.kpis().normalChangeRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("No.36 최근 결과 조회 - Port에 limit=5로 호출됨")
    void recentResultsLimitedToFive() {
        when(loadDashboardOverviewPort.loadDecisionCounts(any(LocalDateTime.class), any(LocalDateTime.class), eq(100L)))
                .thenReturn(new DashboardDecisionCounts(0, 0, 0, 0));
        DashboardOverviewResult.RecentResult sample = new DashboardOverviewResult.RecentResult(
                3001L, 2001L, LocalDateTime.now(), "Press-1",
                "UPLOAD", "DEFECT", "이상", 0.91, "공장A");
        when(loadDashboardOverviewPort.loadRecentResults(eq(100L), eq(5)))
                .thenReturn(List.of(sample));

        DashboardOverviewResult result = service.execute(
                new DashboardOverviewQuery(LocalDate.of(2026, 4, 28), LocalDate.of(2026, 5, 4), null));

        assertThat(result.recentResults()).hasSize(1);
        assertThat(result.recentResults().get(0).resultId()).isEqualTo(3001L);
        assertThat(result.recentResults().get(0).decision()).isEqualTo("DEFECT");
    }
}
