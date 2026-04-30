package com.example.factoryguard.adapter.out.persistence.result;

import com.example.factoryguard.application.dto.result.AnomalyRegionResponse;
import com.example.factoryguard.application.dto.result.ListInspectionResultsQuery;
import com.example.factoryguard.application.dto.result.RelatedResultResponse;
import com.example.factoryguard.application.dto.result.ResultArtifactResponse;
import com.example.factoryguard.application.dto.result.ResultDecisionResponse;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.dto.result.ResultEventLogResponse;
import com.example.factoryguard.application.dto.result.ResultImageResponse;
import com.example.factoryguard.application.dto.result.ResultInspectionResponse;
import com.example.factoryguard.application.dto.result.ResultListSummaryResponse;
import com.example.factoryguard.application.dto.result.ResultPageResponse;
import com.example.factoryguard.application.dto.result.ResultSummaryResponse;
import com.example.factoryguard.application.dto.result.ResultTargetResponse;
import com.example.factoryguard.application.dto.result.ReviewQueueSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ResultQueryRepository {

    private final EntityManager entityManager;

    public ResultPageResponse findPage(ListInspectionResultsQuery query, Long organizationId) {
        SqlParts sql = buildListSql(query, organizationId, false);
        Query nativeQuery = entityManager.createNativeQuery(sql.value());
        bindParameters(nativeQuery, sql.parameters());
        nativeQuery.setFirstResult(query.getPage() * query.getSize());
        nativeQuery.setMaxResults(query.getSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();
        List<ResultSummaryResponse> content = rows.stream()
                .map(this::toSummary)
                .toList();

        SqlParts countSql = buildListSql(query, organizationId, true);
        Query countQuery = entityManager.createNativeQuery(countSql.value());
        bindParameters(countQuery, countSql.parameters());
        long totalElements = toLong(countQuery.getSingleResult());
        int totalPages = query.getSize() == 0 ? 0 : (int) Math.ceil((double) totalElements / query.getSize());
        ResultListSummaryResponse summary = findSummary(query, organizationId);

        return ResultPageResponse.builder()
                .content(content)
                .page(query.getPage())
                .size(query.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .summary(summary)
                .build();
    }

    public Optional<Long> findOrganizationIdByResultId(Long resultId) {
        Query query = entityManager.createNativeQuery("""
                SELECT ir.organization_id
                FROM inspection_result r
                JOIN inspection_run ir ON r.inspection_id = ir.inspection_id
                WHERE r.result_id = :resultId
                """);
        query.setParameter("resultId", resultId);
        @SuppressWarnings("unchecked")
        List<Object> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(toLongObject(rows.get(0)));
    }

    public Optional<ResultDetailResponse> findDetail(Long resultId) {
        Query query = entityManager.createNativeQuery("""
                SELECT
                    r.result_id,
                    r.inspection_id,
                    ir.target_id,
                    at.target_name,
                    at.equipment_name,
                    at.product_name,
                    ir.run_type,
                    ir.input_type,
                    ir.source_type,
                    ir.run_status,
                    ir.started_at,
                    ir.completed_at,
                    r.score,
                    r.confidence,
                    r.decision_code,
                    r.final_decision_code,
                    r.result_status,
                    r.threshold_source,
                    ir.applied_threshold,
                    r.model_version_id,
                    r.failure_reason,
                    r.created_at,
                    rq.queue_status,
                    rq.queued_reason,
                    ir.organization_id
                FROM inspection_result r
                JOIN inspection_run ir ON r.inspection_id = ir.inspection_id
                LEFT JOIN analysis_target at ON ir.target_id = at.target_id
                LEFT JOIN review_queue rq ON r.result_id = rq.result_id
                WHERE r.result_id = :resultId
                """);
        query.setParameter("resultId", resultId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object[] row = rows.get(0);
        Long detailResultId = toLongObject(row[0]);
        List<ResultImageResponse> images = findImages(detailResultId);

        ResultDetailResponse response = ResultDetailResponse.builder()
                .resultId(detailResultId)
                .inspectionId(toLongObject(row[1]))
                .target(ResultTargetResponse.builder()
                        .targetId(toLongObject(row[2]))
                        .targetName(toStringObject(row[3]))
                        .equipmentName(toStringObject(row[4]))
                        .productName(toStringObject(row[5]))
                        .build())
                .inspection(ResultInspectionResponse.builder()
                        .runType(toStringObject(row[6]))
                        .inputType(toStringObject(row[7]))
                        .sourceType(toStringObject(row[8]))
                        .runStatus(toStringObject(row[9]))
                        .startedAt(toLocalDateTime(row[10]))
                        .completedAt(toLocalDateTime(row[11]))
                        .build())
                .result(ResultDecisionResponse.builder()
                        .score(toBigDecimal(row[12]))
                        .confidence(toBigDecimal(row[13]))
                        .decisionCode(toApiDecision(toStringObject(row[14])))
                        .finalDecisionCode(toApiDecision(toStringObject(row[15])))
                        .resultStatus(toStringObject(row[16]))
                        .thresholdSource(toStringObject(row[17]))
                        .appliedThreshold(toBigDecimal(row[18]))
                        .modelVersionId(toLongObject(row[19]))
                        .failureReason(toStringObject(row[20]))
                        .createdAt(toLocalDateTime(row[21]))
                        .build())
                .artifacts(findArtifacts(detailResultId))
                .images(images)
                .review(ReviewQueueSummaryResponse.builder()
                        .reviewRequired(isReviewRequired(toStringObject(row[16]), toStringObject(row[22])))
                        .queueStatus(toStringObject(row[22]))
                        .queuedReason(toStringObject(row[23]))
                        .build())
                .eventLogs(findEventLogs(toLongObject(row[1])))
                .relatedResults(findRelatedResults(
                        detailResultId,
                        toLongObject(row[24]),
                        toLongObject(row[2])
                ))
                .build();

        return Optional.of(response);
    }

    private SqlParts buildListSql(ListInspectionResultsQuery query, Long organizationId, boolean count) {
        StringBuilder sql = new StringBuilder();
        if (count) {
            sql.append("""
                    SELECT COUNT(*)
                    FROM inspection_result r
                    JOIN inspection_run ir ON r.inspection_id = ir.inspection_id
                    LEFT JOIN analysis_target at ON ir.target_id = at.target_id
                    LEFT JOIN review_queue rq ON r.result_id = rq.result_id
                    WHERE 1 = 1
                    """);
        } else {
            sql.append("""
                    SELECT
                        r.result_id,
                        r.inspection_id,
                        ir.target_id,
                        at.target_name,
                        at.equipment_name,
                        at.product_name,
                        ir.run_type,
                        ir.input_type,
                        r.score,
                        r.confidence,
                        r.decision_code,
                        r.final_decision_code,
                        r.result_status,
                        CASE WHEN rq.review_queue_id IS NOT NULL OR r.result_status = 'REVIEW_REQUIRED' THEN 1 ELSE 0 END AS review_required,
                        ir.started_at,
                        ir.completed_at,
                        r.created_at
                    FROM inspection_result r
                    JOIN inspection_run ir ON r.inspection_id = ir.inspection_id
                    LEFT JOIN analysis_target at ON ir.target_id = at.target_id
                    LEFT JOIN review_queue rq ON r.result_id = rq.result_id
                    WHERE 1 = 1
                    """);
        }

        Map<String, Object> parameters = new LinkedHashMap<>();
        if (organizationId != null) {
            sql.append(" AND ir.organization_id = :organizationId");
            parameters.put("organizationId", organizationId);
        }
        if (query.getFrom() != null) {
            sql.append(" AND ir.started_at >= :from");
            parameters.put("from", query.getFrom());
        }
        if (query.getTo() != null) {
            sql.append(" AND ir.started_at <= :to");
            parameters.put("to", query.getTo());
        }
        if (hasText(query.getEquipmentName())) {
            sql.append(" AND at.equipment_name LIKE :equipmentName");
            parameters.put("equipmentName", "%" + query.getEquipmentName().trim() + "%");
        }
        if (hasText(query.getProductName())) {
            sql.append(" AND at.product_name LIKE :productName");
            parameters.put("productName", "%" + query.getProductName().trim() + "%");
        }
        if (hasText(query.getKeyword())) {
            sql.append("""
                     AND (
                        at.equipment_name LIKE :keyword
                        OR at.target_name LIKE :keyword
                        OR at.product_name LIKE :keyword
                        OR ir.run_type LIKE :keyword
                        OR ir.input_type LIKE :keyword
                     )
                    """);
            parameters.put("keyword", "%" + query.getKeyword().trim() + "%");
        }
        if (hasText(query.getRunType())) {
            String runType = query.getRunType().trim().toUpperCase(Locale.ROOT);
            if ("UPLOAD".equals(runType)) {
                sql.append(" AND ir.run_type IN ('UPLOAD', 'IMAGE_UPLOAD', 'VIDEO_UPLOAD')");
            } else {
                sql.append(" AND ir.run_type = :runType");
                parameters.put("runType", runType);
            }
        }
        if (query.getTargetId() != null) {
            sql.append(" AND ir.target_id = :targetId");
            parameters.put("targetId", query.getTargetId());
        }
        if (hasText(query.getDecision())) {
            String decision = query.getDecision().trim().toUpperCase(Locale.ROOT);
            sql.append(" AND COALESCE(r.final_decision_code, r.decision_code) = :decision");
            parameters.put("decision", decision);
        }
        if (hasText(query.getResultStatus())) {
            sql.append(" AND r.result_status = :resultStatus");
            parameters.put("resultStatus", query.getResultStatus().trim().toUpperCase(Locale.ROOT));
        }
        if (!count) {
            sql.append(" ORDER BY ir.started_at DESC, r.created_at DESC, r.result_id DESC");
        }

        return new SqlParts(sql.toString(), parameters);
    }

    private ResultListSummaryResponse findSummary(ListInspectionResultsQuery query, Long organizationId) {
        SqlParts base = buildListSql(query, organizationId, true);
        String fromClause = base.value().substring(base.value().indexOf("FROM"));
        String summarySql = """
                SELECT
                    COUNT(*) AS total_count,
                    SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'NORMAL' THEN 1 ELSE 0 END) AS normal_count,
                    SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'DEFECT' THEN 1 ELSE 0 END) AS defect_count,
                    SUM(CASE WHEN COALESCE(r.final_decision_code, r.decision_code) = 'RECHECK' THEN 1 ELSE 0 END) AS retest_count,
                    AVG(r.score) AS avg_score
                """ + fromClause.replaceFirst("SELECT COUNT\\(\\*\\)\\s+", "");

        Query summaryQuery = entityManager.createNativeQuery(summarySql);
        bindParameters(summaryQuery, base.parameters());
        Object[] row = (Object[]) summaryQuery.getSingleResult();
        return ResultListSummaryResponse.builder()
                .totalCount(toLong(row[0]))
                .normalCount(toLongObject(row[1]) == null ? 0 : toLong(row[1]))
                .defectCount(toLongObject(row[2]) == null ? 0 : toLong(row[2]))
                .retestCount(toLongObject(row[3]) == null ? 0 : toLong(row[3]))
                .avgScore(toBigDecimal(row[4]))
                .build();
    }

    private ResultSummaryResponse toSummary(Object[] row) {
        return ResultSummaryResponse.builder()
                .resultId(toLongObject(row[0]))
                .inspectionId(toLongObject(row[1]))
                .targetId(toLongObject(row[2]))
                .targetName(toStringObject(row[3]))
                .equipmentName(toStringObject(row[4]))
                .productName(toStringObject(row[5]))
                .runType(toStringObject(row[6]))
                .inputType(toStringObject(row[7]))
                .score(toBigDecimal(row[8]))
                .confidence(toBigDecimal(row[9]))
                .decisionCode(toApiDecision(toStringObject(row[10])))
                .finalDecisionCode(toApiDecision(toStringObject(row[11])))
                .resultStatus(toStringObject(row[12]))
                .reviewRequired(toBoolean(row[13]))
                .startedAt(toLocalDateTime(row[14]))
                .completedAt(toLocalDateTime(row[15]))
                .createdAt(toLocalDateTime(row[16]))
                .build();
    }

    private List<ResultArtifactResponse> findArtifacts(Long resultId) {
        Query query = entityManager.createNativeQuery("""
                SELECT artifact_id, artifact_type, file_id
                FROM result_artifact
                WHERE result_id = :resultId
                ORDER BY artifact_id
                """);
        query.setParameter("resultId", resultId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> ResultArtifactResponse.builder()
                        .artifactId(toLongObject(row[0]))
                        .artifactType(toStringObject(row[1]))
                        .fileId(toLongObject(row[2]))
                        .build())
                .toList();
    }

    private List<ResultImageResponse> findImages(Long resultId) {
        Query query = entityManager.createNativeQuery("""
                SELECT image_id, file_id, image_role
                FROM image
                WHERE result_id = :resultId
                ORDER BY image_id
                """);
        query.setParameter("resultId", resultId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        List<ResultImageResponse> images = new ArrayList<>();
        for (Object[] row : rows) {
            Long imageId = toLongObject(row[0]);
            images.add(ResultImageResponse.builder()
                    .imageId(imageId)
                    .fileId(toLongObject(row[1]))
                    .imageRole(toStringObject(row[2]))
                    .regions(findRegions(imageId))
                    .build());
        }
        return images;
    }

    private List<AnomalyRegionResponse> findRegions(Long imageId) {
        Query query = entityManager.createNativeQuery("""
                SELECT region_id, label_code, bbox_x, bbox_y, bbox_w, bbox_h, score
                FROM anomaly_region
                WHERE image_id = :imageId
                ORDER BY region_id
                """);
        query.setParameter("imageId", imageId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> AnomalyRegionResponse.builder()
                        .regionId(toLongObject(row[0]))
                        .labelCode(toStringObject(row[1]))
                        .bboxX(toBigDecimal(row[2]))
                        .bboxY(toBigDecimal(row[3]))
                        .bboxW(toBigDecimal(row[4]))
                        .bboxH(toBigDecimal(row[5]))
                        .score(toBigDecimal(row[6]))
                        .build())
                .toList();
    }

    private List<ResultEventLogResponse> findEventLogs(Long inspectionId) {
        Query query = entityManager.createNativeQuery("""
                SELECT event_id, event_type, message, created_at
                FROM inspection_event_log
                WHERE inspection_id = :inspectionId
                ORDER BY created_at DESC, event_id DESC
                """);
        query.setParameter("inspectionId", inspectionId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> ResultEventLogResponse.builder()
                        .eventId(toLongObject(row[0]))
                        .eventType(toStringObject(row[1]))
                        .message(toStringObject(row[2]))
                        .createdAt(toLocalDateTime(row[3]))
                        .build())
                .toList();
    }

    private List<RelatedResultResponse> findRelatedResults(Long resultId, Long organizationId, Long targetId) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    r.result_id,
                    r.created_at,
                    r.score,
                    r.final_decision_code,
                    at.target_name
                FROM inspection_result r
                JOIN inspection_run ir ON r.inspection_id = ir.inspection_id
                LEFT JOIN analysis_target at ON ir.target_id = at.target_id
                WHERE r.result_id <> :resultId
                  AND ir.organization_id = :organizationId
                """);
        if (targetId != null) {
            sql.append(" AND ir.target_id = :targetId");
        }
        sql.append(" ORDER BY ir.started_at DESC, r.created_at DESC, r.result_id DESC");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("resultId", resultId);
        query.setParameter("organizationId", organizationId);
        if (targetId != null) {
            query.setParameter("targetId", targetId);
        }
        query.setMaxResults(5);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> RelatedResultResponse.builder()
                        .resultId(toLongObject(row[0]))
                        .createdAt(toLocalDateTime(row[1]))
                        .score(toBigDecimal(row[2]))
                        .decisionCode(toApiDecision(toStringObject(row[3])))
                        .location(toStringObject(row[4]))
                        .build())
                .toList();
    }

    private void bindParameters(Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isReviewRequired(String resultStatus, String queueStatus) {
        return queueStatus != null || "REVIEW_REQUIRED".equals(resultStatus);
    }

    private String toApiDecision(String decision) {
        return decision;
    }

    private Long toLongObject(Object value) {
        if (value == null) {
            return null;
        }
        return toLong(value);
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
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

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.parseBoolean(value.toString());
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
        return LocalDateTime.parse(value.toString());
    }

    private record SqlParts(String value, Map<String, Object> parameters) {
    }
}
