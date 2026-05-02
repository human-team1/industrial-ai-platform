package com.example.factoryguard.adapter.out.persistence.operation;

import com.example.factoryguard.application.dto.operation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class OperationAdminQueryRepository {

    private final EntityManager entityManager;

    public Optional<SystemStatusResult> findLatestSystemStatus() {
        Query query = entityManager.createNativeQuery("""
                SELECT snapshot_id, cpu_usage, memory_usage, disk_usage, response_time_ms, created_at
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
        BigDecimal cpu = toBigDecimal(row[1]);
        BigDecimal memory = toBigDecimal(row[2]);
        BigDecimal disk = toBigDecimal(row[3]);
        return Optional.of(SystemStatusResult.builder()
                .snapshotId(toLongObject(row[0]))
                .cpuUsage(cpu)
                .memoryUsage(memory)
                .diskUsage(disk)
                .responseTimeMs(toInteger(row[4]))
                .overallStatus(overallStatus(cpu, memory, disk))
                .createdAt(toLocalDateTime(row[5]))
                .build());
    }

    public List<SystemComponentStatusResult> findSystemComponents() {
        Query query = entityManager.createNativeQuery("""
                SELECT s.component_status_id, s.component_type, s.component_name, s.status, s.message,
                       s.cpu_usage, s.memory_usage, s.disk_usage, s.host_name, s.instance_id,
                       s.response_time_ms, s.checked_at, s.created_at
                FROM system_component_status s
                JOIN (
                    SELECT component_type, MAX(checked_at) AS max_checked_at
                    FROM system_component_status
                    GROUP BY component_type
                ) latest ON latest.component_type = s.component_type AND latest.max_checked_at = s.checked_at
                ORDER BY s.component_type, s.component_status_id DESC
                """);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream().map(row -> SystemComponentStatusResult.builder()
                .componentStatusId(toLongObject(row[0]))
                .componentType(toStringObject(row[1]))
                .componentName(toStringObject(row[2]))
                .status(toStringObject(row[3]))
                .message(toStringObject(row[4]))
                .cpuUsage(toBigDecimal(row[5]))
                .memoryUsage(toBigDecimal(row[6]))
                .diskUsage(toBigDecimal(row[7]))
                .hostName(toStringObject(row[8]))
                .instanceId(toStringObject(row[9]))
                .responseTimeMs(toInteger(row[10]))
                .checkedAt(toLocalDateTime(row[11]))
                .createdAt(toLocalDateTime(row[12]))
                .build()).toList();
    }

    public OperationPageResponse<OperationLogResult> findOperationLogs(ListOperationLogsQuery query) {
        SqlParts parts = operationLogSql(query, false);
        Query dataQuery = entityManager.createNativeQuery(parts.value());
        bind(dataQuery, parts.parameters());
        dataQuery.setFirstResult(query.getPage() * query.getSize());
        dataQuery.setMaxResults(query.getSize());
        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();

        SqlParts countParts = operationLogSql(query, true);
        Query countQuery = entityManager.createNativeQuery(countParts.value());
        bind(countQuery, countParts.parameters());
        long total = toLong(countQuery.getSingleResult());

        return page(rows.stream().map(row -> OperationLogResult.builder()
                .operationLogId(toLongObject(row[0]))
                .eventType(toStringObject(row[1]))
                .eventStatus(toStringObject(row[2]))
                .logLevel(toStringObject(row[3]))
                .sourceComponent(toStringObject(row[4]))
                .requestId(toStringObject(row[5]))
                .actorUserId(toLongObject(row[6]))
                .detailMessage(toStringObject(row[7]))
                .relatedPath(toStringObject(row[8]))
                .createdAt(toLocalDateTime(row[9]))
                .build()).toList(), query.getPage(), query.getSize(), total);
    }

    public List<OperationPolicyResult> findOperationPolicies(ListOperationPoliciesQuery query) {
        StringBuilder sql = new StringBuilder("""
                SELECT operation_policy_id, policy_category, policy_key, policy_name, policy_value,
                       value_type, description, is_active, updated_at, updated_by
                FROM operation_policy
                WHERE 1 = 1
                """);
        Map<String, Object> params = new LinkedHashMap<>();
        if (hasText(query.getCategory())) {
            sql.append(" AND policy_category = :category");
            params.put("category", query.getCategory());
        }
        if (query.isActiveOnly()) {
            sql.append(" AND is_active = TRUE");
        }
        sql.append(" ORDER BY policy_category, policy_key");
        Query nativeQuery = entityManager.createNativeQuery(sql.toString());
        bind(nativeQuery, params);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();
        return rows.stream().map(this::toPolicy).toList();
    }

    public Optional<OperationPolicyResult> findOperationPolicy(Long policyId) {
        Query query = entityManager.createNativeQuery("""
                SELECT operation_policy_id, policy_category, policy_key, policy_name, policy_value,
                       value_type, description, is_active, updated_at, updated_by
                FROM operation_policy
                WHERE operation_policy_id = :policyId
                """);
        query.setParameter("policyId", policyId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream().findFirst().map(this::toPolicy);
    }

    public OperationPolicyResult updateOperationPolicy(Long policyId, String policyValue, Boolean isActive, Long updatedBy) {
        Query query = entityManager.createNativeQuery("""
                UPDATE operation_policy
                SET policy_value = :policyValue,
                    is_active = COALESCE(:isActive, is_active),
                    updated_by = :updatedBy,
                    updated_at = CURRENT_TIMESTAMP
                WHERE operation_policy_id = :policyId
                """);
        query.setParameter("policyValue", policyValue);
        query.setParameter("isActive", isActive);
        query.setParameter("updatedBy", updatedBy);
        query.setParameter("policyId", policyId);
        query.executeUpdate();
        return findOperationPolicy(policyId).orElseThrow();
    }

    public OperationPageResponse<AuditLogResult> findAuditLogs(ListAuditLogsQuery query) {
        SqlParts parts = auditLogSql(query, false);
        Query dataQuery = entityManager.createNativeQuery(parts.value());
        bind(dataQuery, parts.parameters());
        dataQuery.setFirstResult(query.getPage() * query.getSize());
        dataQuery.setMaxResults(query.getSize());
        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        long total = count(auditLogSql(query, true));
        return page(rows.stream().map(row -> AuditLogResult.builder()
                .auditLogId(toLongObject(row[0]))
                .actorUserId(toLongObject(row[1]))
                .actionType(toStringObject(row[2]))
                .targetType(toStringObject(row[3]))
                .targetId(toLongObject(row[4]))
                .createdAt(toLocalDateTime(row[5]))
                .build()).toList(), query.getPage(), query.getSize(), total);
    }

    public OperationPageResponse<AdminActionLogResult> findAdminActionLogs(ListAuditLogsQuery query) {
        SqlParts parts = adminActionLogSql(query, false);
        Query dataQuery = entityManager.createNativeQuery(parts.value());
        bind(dataQuery, parts.parameters());
        dataQuery.setFirstResult(query.getPage() * query.getSize());
        dataQuery.setMaxResults(query.getSize());
        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        long total = count(adminActionLogSql(query, true));
        return page(rows.stream().map(row -> AdminActionLogResult.builder()
                .adminActionId(toLongObject(row[0]))
                .actorUserId(toLongObject(row[1]))
                .actionType(toStringObject(row[2]))
                .targetType(toStringObject(row[3]))
                .targetId(toLongObject(row[4]))
                .reason(toStringObject(row[5]))
                .createdAt(toLocalDateTime(row[6]))
                .build()).toList(), query.getPage(), query.getSize(), total);
    }

    public OperationPageResponse<AsyncJobSummary> findAsyncJobs(ListAsyncJobsQuery query) {
        SqlParts parts = asyncJobSql(query, false);
        Query dataQuery = entityManager.createNativeQuery(parts.value());
        bind(dataQuery, parts.parameters());
        dataQuery.setFirstResult(query.getPage() * query.getSize());
        dataQuery.setMaxResults(query.getSize());
        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        long total = count(asyncJobSql(query, true));
        return page(rows.stream().map(row -> AsyncJobSummary.builder()
                .jobId(toLongObject(row[0]))
                .jobType(toStringObject(row[1]))
                .jobStatus(toStringObject(row[2]))
                .targetType(toStringObject(row[3]))
                .targetId(toLongObject(row[4]))
                .errorMessage(toStringObject(row[5]))
                .createdAt(toLocalDateTime(row[6]))
                .completedAt(toLocalDateTime(row[7]))
                .build()).toList(), query.getPage(), query.getSize(), total);
    }

    public Optional<AsyncJobDetail> findAsyncJob(Long jobId) {
        Query query = entityManager.createNativeQuery("""
                SELECT job_id, job_type, job_status, target_type, target_id, error_message, created_at, completed_at
                FROM async_job
                WHERE job_id = :jobId
                """);
        query.setParameter("jobId", jobId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream().findFirst().map(row -> AsyncJobDetail.builder()
                .jobId(toLongObject(row[0]))
                .jobType(toStringObject(row[1]))
                .jobStatus(toStringObject(row[2]))
                .targetType(toStringObject(row[3]))
                .targetId(toLongObject(row[4]))
                .errorMessage(toStringObject(row[5]))
                .createdAt(toLocalDateTime(row[6]))
                .completedAt(toLocalDateTime(row[7]))
                .build());
    }

    public void saveAdminActionLog(Long actorUserId, String actionType, String targetType, Long targetId, String reason) {
        entityManager.createNativeQuery("""
                INSERT INTO admin_action_log(actor_user_id, action_type, target_type, target_id, reason, created_at)
                VALUES(:actorUserId, :actionType, :targetType, :targetId, :reason, CURRENT_TIMESTAMP)
                """)
                .setParameter("actorUserId", actorUserId)
                .setParameter("actionType", actionType)
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .setParameter("reason", reason)
                .executeUpdate();
    }

    public void saveAuditLog(Long actorUserId, String actionType, String targetType, Long targetId, String beforeJson, String afterJson) {
        entityManager.createNativeQuery("""
                INSERT INTO audit_log(actor_user_id, action_type, target_type, target_id, before_json, after_json, created_at)
                VALUES(:actorUserId, :actionType, :targetType, :targetId, :beforeJson, :afterJson, CURRENT_TIMESTAMP)
                """)
                .setParameter("actorUserId", actorUserId)
                .setParameter("actionType", actionType)
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .setParameter("beforeJson", beforeJson)
                .setParameter("afterJson", afterJson)
                .executeUpdate();
    }

    public void saveOperationLog(String eventType, String eventStatus, String logLevel, String sourceComponent,
                                 String requestId, Long actorUserId, String detailMessage, String relatedPath) {
        entityManager.createNativeQuery("""
                INSERT INTO operation_log(event_type, event_status, log_level, source_component, request_id,
                                          actor_user_id, detail_message, related_path, created_at)
                VALUES(:eventType, :eventStatus, :logLevel, :sourceComponent, :requestId,
                       :actorUserId, :detailMessage, :relatedPath, CURRENT_TIMESTAMP)
                """)
                .setParameter("eventType", eventType)
                .setParameter("eventStatus", eventStatus)
                .setParameter("logLevel", logLevel)
                .setParameter("sourceComponent", sourceComponent)
                .setParameter("requestId", requestId)
                .setParameter("actorUserId", actorUserId)
                .setParameter("detailMessage", detailMessage)
                .setParameter("relatedPath", relatedPath)
                .executeUpdate();
    }

    public void saveSystemStatusSnapshot(SystemComponentStatusResult status) {
        entityManager.createNativeQuery("""
                INSERT INTO system_status_snapshot(cpu_usage, memory_usage, disk_usage, response_time_ms, created_at)
                VALUES(:cpuUsage, :memoryUsage, :diskUsage, :responseTimeMs, :createdAt)
                """)
                .setParameter("cpuUsage", status.getCpuUsage())
                .setParameter("memoryUsage", status.getMemoryUsage())
                .setParameter("diskUsage", status.getDiskUsage())
                .setParameter("responseTimeMs", status.getResponseTimeMs())
                .setParameter("createdAt", status.getCheckedAt() != null ? status.getCheckedAt() : LocalDateTime.now())
                .executeUpdate();
    }

    public void saveSystemComponentStatus(SystemComponentStatusResult status) {
        entityManager.createNativeQuery("""
                INSERT INTO system_component_status(
                    component_type, component_name, status, message, cpu_usage, memory_usage, disk_usage,
                    host_name, instance_id, response_time_ms, checked_at, created_at
                )
                VALUES(
                    :componentType, :componentName, :status, :message, :cpuUsage, :memoryUsage, :diskUsage,
                    :hostName, :instanceId, :responseTimeMs, :checkedAt, CURRENT_TIMESTAMP
                )
                """)
                .setParameter("componentType", status.getComponentType())
                .setParameter("componentName", status.getComponentName())
                .setParameter("status", status.getStatus())
                .setParameter("message", status.getMessage())
                .setParameter("cpuUsage", status.getCpuUsage())
                .setParameter("memoryUsage", status.getMemoryUsage())
                .setParameter("diskUsage", status.getDiskUsage())
                .setParameter("hostName", status.getHostName())
                .setParameter("instanceId", status.getInstanceId())
                .setParameter("responseTimeMs", status.getResponseTimeMs())
                .setParameter("checkedAt", status.getCheckedAt() != null ? status.getCheckedAt() : LocalDateTime.now())
                .executeUpdate();
    }

    private SqlParts operationLogSql(ListOperationLogsQuery query, boolean count) {
        String select = count ? "SELECT COUNT(*)" : """
                SELECT operation_log_id, event_type, event_status, log_level, source_component,
                       request_id, actor_user_id, detail_message, related_path, created_at
                """;
        StringBuilder sql = new StringBuilder(select + " FROM operation_log WHERE 1 = 1");
        Map<String, Object> params = new LinkedHashMap<>();
        addText(sql, params, "log_level", "level", query.getLevel());
        addText(sql, params, "source_component", "sourceComponent", query.getSourceComponent());
        addText(sql, params, "event_status", "eventStatus", query.getEventStatus());
        addText(sql, params, "event_type", "eventType", query.getEventType());
        addDateRange(sql, params, query.getStartDate(), query.getEndDate());
        if (!count) {
            sql.append(" ORDER BY created_at ").append("asc".equalsIgnoreCase(query.getSort()) ? "ASC" : "DESC")
                    .append(", operation_log_id DESC");
        }
        return new SqlParts(sql.toString(), params);
    }

    private SqlParts auditLogSql(ListAuditLogsQuery query, boolean count) {
        String select = count ? "SELECT COUNT(*)" : "SELECT audit_log_id, actor_user_id, action_type, target_type, target_id, created_at";
        return logSql(select, "audit_log", "audit_log_id", query, count);
    }

    private SqlParts adminActionLogSql(ListAuditLogsQuery query, boolean count) {
        String select = count ? "SELECT COUNT(*)" : "SELECT admin_action_id, actor_user_id, action_type, target_type, target_id, reason, created_at";
        return logSql(select, "admin_action_log", "admin_action_id", query, count);
    }

    private SqlParts asyncJobSql(ListAsyncJobsQuery query, boolean count) {
        String select = count ? "SELECT COUNT(*)" : "SELECT job_id, job_type, job_status, target_type, target_id, error_message, created_at, completed_at";
        StringBuilder sql = new StringBuilder(select + " FROM async_job WHERE 1 = 1");
        Map<String, Object> params = new LinkedHashMap<>();
        addText(sql, params, "job_status", "jobStatus", query.getJobStatus());
        addText(sql, params, "job_type", "jobType", query.getJobType());
        if (!count) {
            sql.append(" ORDER BY created_at DESC, job_id DESC");
        }
        return new SqlParts(sql.toString(), params);
    }

    private SqlParts logSql(String select, String table, String idColumn, ListAuditLogsQuery query, boolean count) {
        StringBuilder sql = new StringBuilder(select + " FROM " + table + " WHERE 1 = 1");
        Map<String, Object> params = new LinkedHashMap<>();
        if (query.getActorUserId() != null) {
            sql.append(" AND actor_user_id = :actorUserId");
            params.put("actorUserId", query.getActorUserId());
        }
        addText(sql, params, "action_type", "actionType", query.getActionType());
        addText(sql, params, "target_type", "targetType", query.getTargetType());
        addDateRange(sql, params, query.getStartDate(), query.getEndDate());
        if (!count) {
            sql.append(" ORDER BY created_at DESC, ").append(idColumn).append(" DESC");
        }
        return new SqlParts(sql.toString(), params);
    }

    private void addText(StringBuilder sql, Map<String, Object> params, String column, String key, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" = :").append(key);
            params.put(key, value);
        }
    }

    private void addDateRange(StringBuilder sql, Map<String, Object> params, String startDate, String endDate) {
        if (hasText(startDate)) {
            sql.append(" AND created_at >= :startDate");
            params.put("startDate", LocalDate.parse(startDate).atStartOfDay());
        }
        if (hasText(endDate)) {
            sql.append(" AND created_at < :endDate");
            params.put("endDate", LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }
    }

    private long count(SqlParts parts) {
        Query query = entityManager.createNativeQuery(parts.value());
        bind(query, parts.parameters());
        return toLong(query.getSingleResult());
    }

    private <T> OperationPageResponse<T> page(List<T> content, int page, int size, long total) {
        return OperationPageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(size == 0 ? 0 : (int) Math.ceil((double) total / size))
                .build();
    }

    private OperationPolicyResult toPolicy(Object[] row) {
        return OperationPolicyResult.builder()
                .operationPolicyId(toLongObject(row[0]))
                .policyCategory(toStringObject(row[1]))
                .policyKey(toStringObject(row[2]))
                .policyName(toStringObject(row[3]))
                .policyValue(toStringObject(row[4]))
                .valueType(toStringObject(row[5]))
                .description(toStringObject(row[6]))
                .isActive(toBoolean(row[7]))
                .updatedAt(toLocalDateTime(row[8]))
                .updatedBy(toLongObject(row[9]))
                .build();
    }

    private void bind(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String overallStatus(BigDecimal cpu, BigDecimal memory, BigDecimal disk) {
        BigDecimal max = List.of(cpu, memory, disk).stream().filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        if (max.compareTo(BigDecimal.valueOf(90)) >= 0) return "ERROR";
        if (max.compareTo(BigDecimal.valueOf(80)) >= 0) return "WARNING";
        return "NORMAL";
    }

    private Long toLongObject(Object value) {
        return value == null ? null : toLong(value);
    }

    private long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
    }

    private Integer toInteger(Object value) {
        return value == null ? null : (value instanceof Number number ? number.intValue() : Integer.parseInt(value.toString()));
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal decimal) return decimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return new BigDecimal(value.toString());
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Number number) return number.intValue() != 0;
        return Boolean.parseBoolean(value.toString());
    }

    private String toStringObject(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime dateTime) return dateTime;
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        return LocalDateTime.parse(value.toString().replace(" ", "T"));
    }

    private record SqlParts(String value, Map<String, Object> parameters) {
    }
}
