package com.example.factoryguard.adapter.out.persistence.dashboard;

import com.example.factoryguard.application.dto.dashboard.DashboardDecisionCounts;
import com.example.factoryguard.application.dto.dashboard.DashboardOverviewResult;
import com.example.factoryguard.application.dto.dashboard.DashboardSummaryMetrics;
import com.example.factoryguard.application.dto.dashboard.DashboardSystemSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DashboardOverviewQueryRepository {

    private final EntityManager entityManager;

    public DashboardDecisionCounts loadDecisionCounts(LocalDateTime from, LocalDateTime to, Long organizationId) {
        SqlParts sql = withOrganization("""
                SELECT
                    COUNT(*) AS total_count,
                    SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'DEFECT' THEN 1 ELSE 0 END) AS anomaly_count,
                    SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'NORMAL' THEN 1 ELSE 0 END) AS normal_count,
                    SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'RECHECK' THEN 1 ELSE 0 END) AS recheck_count
                FROM inspection_run ir
                JOIN inspection_result r ON r.inspection_id = ir.inspection_id
                WHERE r.result_status IN ('SUCCESS', 'REVIEW_REQUIRED', 'FAILED')
                  AND COALESCE(r.created_at, ir.started_at) >= :from
                  AND COALESCE(r.created_at, ir.started_at) < :to
                """, organizationId);
        Query query = entityManager.createNativeQuery(sql.value());
        query.setParameter("from", from);
        query.setParameter("to", to);
        bind(query, sql.parameters());
        Object[] row = (Object[]) query.getSingleResult();
        return new DashboardDecisionCounts(toLong(row[0]), toLong(row[1]), toLong(row[2]), toLong(row[3]));
    }

    public List<DashboardOverviewResult.TrendPoint> loadTrend(LocalDateTime from, LocalDateTime to, Long organizationId) {
        SqlParts sql = withOrganization("""
                SELECT
                    DATE(COALESCE(r.created_at, ir.started_at)) AS inspected_date,
                    SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'NORMAL' THEN 1 ELSE 0 END) AS normal_count,
                    SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'DEFECT' THEN 1 ELSE 0 END) AS anomaly_count,
                    SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'RECHECK' THEN 1 ELSE 0 END) AS recheck_count,
                    COUNT(*) AS total_count
                FROM inspection_run ir
                JOIN inspection_result r ON r.inspection_id = ir.inspection_id
                WHERE r.result_status IN ('SUCCESS', 'REVIEW_REQUIRED', 'FAILED')
                  AND COALESCE(r.created_at, ir.started_at) >= :from
                  AND COALESCE(r.created_at, ir.started_at) < :to
                """, organizationId, """
                GROUP BY DATE(COALESCE(r.created_at, ir.started_at))
                ORDER BY inspected_date
                """);
        Query query = entityManager.createNativeQuery(sql.value());
        query.setParameter("from", from);
        query.setParameter("to", to);
        bind(query, sql.parameters());
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> new DashboardOverviewResult.TrendPoint(
                        toLocalDate(row[0]),
                        toLong(row[1]),
                        toLong(row[2]),
                        toLong(row[3]),
                        rate(toLong(row[2]), toLong(row[4]))
                ))
                .toList();
    }

    public List<DashboardOverviewResult.TopEquipmentAnomalyRate> loadTopEquipmentAnomalyRates(
            LocalDateTime from,
            LocalDateTime to,
            Long organizationId,
            int limit
    ) {
        SqlParts sql = withOrganization("""
                SELECT
                    at.target_id,
                    COALESCE(at.equipment_name, at.target_name, '검사대상 없음') AS equipment_name,
                    COUNT(*) AS inspection_count,
                    SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'DEFECT' THEN 1 ELSE 0 END) AS anomaly_count
                FROM inspection_run ir
                JOIN inspection_result r ON r.inspection_id = ir.inspection_id
                LEFT JOIN analysis_target at ON at.target_id = ir.target_id
                WHERE r.result_status IN ('SUCCESS', 'REVIEW_REQUIRED', 'FAILED')
                  AND COALESCE(r.created_at, ir.started_at) >= :from
                  AND COALESCE(r.created_at, ir.started_at) < :to
                """, organizationId, """
                GROUP BY at.target_id, COALESCE(at.equipment_name, at.target_name, '검사대상 없음')
                ORDER BY (SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'DEFECT' THEN 1 ELSE 0 END) * 1.0 / COUNT(*)) DESC,
                         anomaly_count DESC,
                         inspection_count DESC
                """);
        Query query = entityManager.createNativeQuery(sql.value());
        query.setParameter("from", from);
        query.setParameter("to", to);
        bind(query, sql.parameters());
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> {
                    long inspectionCount = toLong(row[2]);
                    long anomalyCount = toLong(row[3]);
                    return new DashboardOverviewResult.TopEquipmentAnomalyRate(
                            toLongObject(row[0]),
                            toStringObject(row[1]),
                            inspectionCount,
                            anomalyCount,
                            rate(anomalyCount, inspectionCount)
                    );
                })
                .toList();
    }

    public List<DashboardOverviewResult.RecentResult> loadRecentResults(Long organizationId, int limit) {
        SqlParts sql = withOrganization("""
                SELECT
                    r.result_id,
                    ir.inspection_id,
                    COALESCE(r.created_at, ir.started_at) AS inspected_at,
                    COALESCE(at.equipment_name, at.target_name, '검사대상 없음') AS equipment_name,
                    ir.run_type,
                    COALESCE(r.final_decision_code, r.decision_code) AS decision_code,
                    r.score,
                    at.location_name
                FROM inspection_result r
                JOIN inspection_run ir ON r.inspection_id = ir.inspection_id
                LEFT JOIN analysis_target at ON at.target_id = ir.target_id
                WHERE r.result_status IN ('SUCCESS', 'REVIEW_REQUIRED', 'FAILED')
                """, organizationId, """
                ORDER BY COALESCE(r.created_at, ir.started_at) DESC, r.result_id DESC
                """);
        Query query = entityManager.createNativeQuery(sql.value());
        bind(query, sql.parameters());
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> {
                    String decision = toStringObject(row[5]);
                    return new DashboardOverviewResult.RecentResult(
                            toLongObject(row[0]),
                            toLongObject(row[1]),
                            toLocalDateTime(row[2]),
                            toStringObject(row[3]),
                            inspectionTypeLabel(toStringObject(row[4])),
                            decision,
                            decisionLabel(decision),
                            toDoubleObject(row[6]),
                            toStringObject(row[7])
                    );
                })
                .toList();
    }

    public List<DashboardOverviewResult.RecentNotification> loadRecentNotifications(Long organizationId, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT n.notification_id, n.severity, n.title, n.created_at, n.target_url
                FROM notification n
                JOIN users u ON u.user_id = n.user_id
                WHERE 1 = 1
                """);
        Map<String, Object> parameters = new LinkedHashMap<>();
        if (organizationId != null) {
            sql.append(" AND u.organization_id = :organizationId");
            parameters.put("organizationId", organizationId);
        }
        sql.append(" ORDER BY n.created_at DESC, n.notification_id DESC");
        Query query = entityManager.createNativeQuery(sql.toString());
        bind(query, parameters);
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> new DashboardOverviewResult.RecentNotification(
                        toLongObject(row[0]),
                        toStringObject(row[1]),
                        toStringObject(row[2]),
                        toLocalDateTime(row[3]),
                        toStringObject(row[4])
                ))
                .toList();
    }

    public DashboardSummaryMetrics loadSummary(Long organizationId) {
        String targetFilter = organizationId == null ? "" : " AND at.organization_id = :organizationId";
        long registeredTargetCount = singleLong(new SqlParts("""
                SELECT COUNT(*)
                FROM analysis_target at
                WHERE at.target_status = 'ACTIVE'
                """ + targetFilter,
                organizationId == null ? Map.of() : Map.of("organizationId", organizationId)));

        long activeModelCount = singleLong(new SqlParts("""
                SELECT COUNT(*)
                FROM model_version
                WHERE is_active = TRUE
                """, Map.of()));

        SqlParts fileSql = new SqlParts("""
                SELECT COALESCE(SUM(f.file_size), 0)
                FROM file f
                LEFT JOIN users u ON u.user_id = f.created_by
                WHERE 1 = 1
                """ + (organizationId == null ? "" : " AND u.organization_id = :organizationId"),
                organizationId == null ? Map.of() : Map.of("organizationId", organizationId));
        long totalDataSizeBytes = singleLong(fileSql);

        LocalDate latestTrainingDate = singleDate(new SqlParts("""
                SELECT DATE(MAX(validated_at))
                FROM model_version
                WHERE validated_at IS NOT NULL
                """, Map.of())).orElse(null);

        return new DashboardSummaryMetrics(registeredTargetCount, activeModelCount, totalDataSizeBytes, latestTrainingDate);
    }

    public Optional<DashboardSystemSnapshot> loadLatestSystemSnapshot() {
        Query query = entityManager.createNativeQuery("""
                SELECT cpu_usage, memory_usage, disk_usage, response_time_ms, created_at
                FROM system_status_snapshot
                ORDER BY created_at DESC, snapshot_id DESC
                """);
        query.setMaxResults(1);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] row = rows.get(0);
        return Optional.of(new DashboardSystemSnapshot(
                toBigDecimal(row[0]),
                toBigDecimal(row[1]),
                toBigDecimal(row[2]),
                toIntegerObject(row[3]),
                toLocalDateTime(row[4])
        ));
    }

    private SqlParts withOrganization(String baseSql, Long organizationId) {
        return withOrganization(baseSql, organizationId, "");
    }

    private SqlParts withOrganization(String baseSql, Long organizationId, String suffix) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder(baseSql);
        if (organizationId != null) {
            sql.append(" AND ir.organization_id = :organizationId");
            parameters.put("organizationId", organizationId);
        }
        sql.append(suffix);
        return new SqlParts(sql.toString(), parameters);
    }

    private long singleLong(SqlParts sql) {
        Query query = entityManager.createNativeQuery(sql.value());
        bind(query, sql.parameters());
        return toLong(query.getSingleResult());
    }

    private Optional<LocalDate> singleDate(SqlParts sql) {
        Query query = entityManager.createNativeQuery(sql.value());
        bind(query, sql.parameters());
        @SuppressWarnings("unchecked")
        List<Object> rows = query.getResultList();
        if (rows.isEmpty() || rows.get(0) == null) {
            return Optional.empty();
        }
        return Optional.of(toLocalDate(rows.get(0)));
    }

    private void bind(Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private double rate(long value, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(((double) value / total * 100.0) * 100.0) / 100.0;
    }

    private String decisionLabel(String decision) {
        if ("DEFECT".equals(decision)) {
            return "이상";
        }
        if ("NORMAL".equals(decision)) {
            return "정상";
        }
        if ("RECHECK".equals(decision)) {
            return "재검사";
        }
        return decision;
    }

    private String inspectionTypeLabel(String runType) {
        if ("REALTIME".equals(runType)) {
            return "실시간 탐지";
        }
        if ("UPLOAD".equals(runType)) {
            return "업로드 검사";
        }
        return runType;
    }

    private Long toLongObject(Object value) {
        return value == null ? null : toLong(value);
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private Double toDoubleObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    private Integer toIntegerObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private String toStringObject(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return LocalDateTime.parse(value.toString().replace(" ", "T"));
    }

    private record SqlParts(String value, Map<String, Object> parameters) {
    }
}
