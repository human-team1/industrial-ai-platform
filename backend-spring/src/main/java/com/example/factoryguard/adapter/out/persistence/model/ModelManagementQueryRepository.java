package com.example.factoryguard.adapter.out.persistence.model;

import com.example.factoryguard.application.dto.inspection.AvailableInspectionModelItem;
import com.example.factoryguard.application.dto.model.ListModelDeploymentsQuery;
import com.example.factoryguard.application.dto.model.ListModelVersionsQuery;
import com.example.factoryguard.application.dto.model.ListModelsQuery;
import com.example.factoryguard.application.dto.model.ModelArtifactResponse;
import com.example.factoryguard.application.dto.model.ModelDeploymentResponse;
import com.example.factoryguard.application.dto.model.ModelPageResponse;
import com.example.factoryguard.application.dto.model.ModelSummaryResponse;
import com.example.factoryguard.application.dto.model.ModelVersionSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ModelManagementQueryRepository {

    private final EntityManager entityManager;

    public ModelPageResponse<ModelSummaryResponse> findModels(ListModelsQuery query) {
        SqlParts dataSql = buildModelsSql(query, false);
        Query nativeQuery = entityManager.createNativeQuery(dataSql.value());
        bind(nativeQuery, dataSql.parameters());
        nativeQuery.setFirstResult(query.getPage() * query.getSize());
        nativeQuery.setMaxResults(query.getSize());
        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();
        List<ModelSummaryResponse> content = rows.stream().map(this::toModelSummary).toList();
        long total = count(buildModelsSql(query, true));
        return toPage(content, query.getPage(), query.getSize(), total);
    }

    public ModelPageResponse<ModelVersionSummaryResponse> findModelVersions(ListModelVersionsQuery query) {
        SqlParts dataSql = buildVersionsSql(query, false);
        Query nativeQuery = entityManager.createNativeQuery(dataSql.value());
        bind(nativeQuery, dataSql.parameters());
        nativeQuery.setFirstResult(query.getPage() * query.getSize());
        nativeQuery.setMaxResults(query.getSize());
        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();
        List<ModelVersionSummaryResponse> content = rows.stream().map(this::toVersionSummary).toList();
        long total = count(buildVersionsSql(query, true));
        return toPage(content, query.getPage(), query.getSize(), total);
    }

    public ModelPageResponse<ModelDeploymentResponse> findDeployments(ListModelDeploymentsQuery query) {
        SqlParts dataSql = buildDeploymentsSql(query, false);
        Query nativeQuery = entityManager.createNativeQuery(dataSql.value());
        bind(nativeQuery, dataSql.parameters());
        nativeQuery.setFirstResult(query.getPage() * query.getSize());
        nativeQuery.setMaxResults(query.getSize());
        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();
        List<ModelDeploymentResponse> content = rows.stream().map(this::toDeployment).toList();
        long total = count(buildDeploymentsSql(query, true));
        return toPage(content, query.getPage(), query.getSize(), total);
    }

    public List<ModelArtifactResponse> findArtifactsByVersionId(Long versionId) {
        Query query = entityManager.createNativeQuery("""
                SELECT ma.model_artifact_id, ma.model_version_id, ma.file_id, ma.artifact_type,
                       f.file_name, f.object_key, ma.checksum, ma.created_at
                FROM model_artifact ma
                LEFT JOIN file f ON ma.file_id = f.file_id
                WHERE ma.model_version_id = :versionId
                ORDER BY ma.created_at ASC, ma.model_artifact_id ASC
                """);
        query.setParameter("versionId", versionId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> ModelArtifactResponse.builder()
                        .modelArtifactId(toLong(row[0]))
                        .modelVersionId(toLong(row[1]))
                        .fileId(toLong(row[2]))
                        .artifactType(toStringValue(row[3]))
                        .fileName(toStringValue(row[4]))
                        .objectKey(toStringValue(row[5]))
                        .checksum(toStringValue(row[6]))
                        .createdAt(toDateTime(row[7]))
                        .build())
                .toList();
    }

    public List<AvailableInspectionModelItem> findAvailableInspectionModels(Long organizationId, Long targetId, String modelCategory) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    md.deployment_id,
                    mv.model_version_id,
                    mv.model_id,
                    m.model_name,
                    mv.version_name,
                    mv.model_category,
                    mv.model_profile,
                    md.deployment_scope,
                    md.organization_id,
                    md.target_id,
                    mv.deploy_status AS version_deploy_status,
                    md.deploy_status AS deployment_deploy_status,
                    md.is_active,
                    mv.threshold_default,
                    md.deployed_at
                FROM model_deployment md
                JOIN model_version mv ON md.model_version_id = mv.model_version_id
                JOIN model m ON mv.model_id = m.model_id
                WHERE md.organization_id = :organizationId
                  AND md.is_active = true
                  AND mv.is_active = true
                  AND md.deploy_status = 'DEPLOYED'
                  AND mv.deploy_status = 'DEPLOYED'
                """);

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("organizationId", organizationId);

        if (targetId != null) {
            sql.append(" AND (md.target_id = :targetId OR (md.target_id IS NULL AND md.deployment_scope = 'ORGANIZATION'))");
            parameters.put("targetId", targetId);
        } else {
            sql.append(" AND md.deployment_scope = 'ORGANIZATION'");
        }
        if (hasText(modelCategory)) {
            sql.append(" AND mv.model_category = :modelCategory");
            parameters.put("modelCategory", modelCategory.trim());
        }
        sql.append("""
                 ORDER BY
                   CASE WHEN md.target_id IS NOT NULL THEN 0 ELSE 1 END,
                   md.deployed_at DESC,
                   mv.model_profile ASC,
                   mv.version_name DESC
                """);

        Query query = entityManager.createNativeQuery(sql.toString());
        bind(query, parameters);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> AvailableInspectionModelItem.builder()
                        .deploymentId(toLong(row[0]))
                        .modelVersionId(toLong(row[1]))
                        .modelId(toLong(row[2]))
                        .modelName(toStringValue(row[3]))
                        .versionName(toStringValue(row[4]))
                        .modelCategory(toStringValue(row[5]))
                        .modelProfile(toStringValue(row[6]))
                        .deploymentScope(toStringValue(row[7]))
                        .organizationId(toLong(row[8]))
                        .targetId(toLong(row[9]))
                        .modelVersionStatus(toStringValue(row[10]))
                        .deploymentStatus(toStringValue(row[11]))
                        .isActive(toBoolean(row[12]))
                        .thresholdDefault(toDecimal(row[13]))
                        .createdAt(toDateTime(row[14]))
                        .build())
                .toList();
    }

    private SqlParts buildModelsSql(ListModelsQuery query, boolean count) {
        StringBuilder sql = new StringBuilder(count
                ? "SELECT COUNT(*) FROM model m WHERE 1 = 1"
                : "SELECT m.model_id, m.model_name, m.model_type, m.description, m.created_at FROM model m WHERE 1 = 1");
        Map<String, Object> parameters = new LinkedHashMap<>();
        if (hasText(query.getModelType())) {
            sql.append(" AND m.model_type = :modelType");
            parameters.put("modelType", query.getModelType().trim());
        }
        if (!count) {
            sql.append(" ORDER BY m.created_at DESC, m.model_id DESC");
        }
        return new SqlParts(sql.toString(), parameters);
    }

    private SqlParts buildVersionsSql(ListModelVersionsQuery query, boolean count) {
        StringBuilder sql = new StringBuilder(count
                ? "SELECT COUNT(*) FROM model_version mv JOIN model m ON mv.model_id = m.model_id WHERE mv.model_id = :modelId"
                : """
                SELECT mv.model_version_id, mv.model_id, m.model_name, mv.version_name, mv.model_category, mv.model_profile,
                       mv.framework, mv.input_size, mv.threshold_default, mv.accuracy, mv.precision_score, mv.recall_score,
                       mv.f1_score, mv.auroc_score, mv.deploy_status, mv.is_active, mv.validated_at, mv.validated_by, mv.created_at
                FROM model_version mv
                JOIN model m ON mv.model_id = m.model_id
                WHERE mv.model_id = :modelId
                """);
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("modelId", query.getModelId());
        if (hasText(query.getModelCategory())) {
            sql.append(" AND mv.model_category = :modelCategory");
            parameters.put("modelCategory", query.getModelCategory().trim());
        }
        if (hasText(query.getModelProfile())) {
            sql.append(" AND mv.model_profile = :modelProfile");
            parameters.put("modelProfile", query.getModelProfile().trim());
        }
        if (hasText(query.getDeployStatus())) {
            sql.append(" AND mv.deploy_status = :deployStatus");
            parameters.put("deployStatus", query.getDeployStatus().trim());
        }
        if (query.getIsActive() != null) {
            sql.append(" AND mv.is_active = :isActive");
            parameters.put("isActive", query.getIsActive());
        }
        if (!count) {
            sql.append(" ORDER BY mv.created_at DESC, mv.model_version_id DESC");
        }
        return new SqlParts(sql.toString(), parameters);
    }

    private SqlParts buildDeploymentsSql(ListModelDeploymentsQuery query, boolean count) {
        StringBuilder sql = new StringBuilder(count
                ? "SELECT COUNT(*) FROM model_deployment md JOIN model_version mv ON md.model_version_id = mv.model_version_id JOIN model m ON mv.model_id = m.model_id WHERE 1 = 1"
                : """
                SELECT md.deployment_id, md.organization_id, md.target_id, md.model_version_id, mv.model_id, m.model_name,
                       mv.version_name, md.deployment_scope, md.deploy_status, md.is_active, md.deployed_at, md.deployed_by,
                       md.rollback_from_deployment_id, md.reason
                FROM model_deployment md
                JOIN model_version mv ON md.model_version_id = mv.model_version_id
                JOIN model m ON mv.model_id = m.model_id
                WHERE 1 = 1
                """);
        Map<String, Object> parameters = new LinkedHashMap<>();
        if (query.getOrganizationId() != null) {
            sql.append(" AND md.organization_id = :organizationId");
            parameters.put("organizationId", query.getOrganizationId());
        }
        if (query.getTargetId() != null) {
            sql.append(" AND md.target_id = :targetId");
            parameters.put("targetId", query.getTargetId());
        }
        if (query.getModelVersionId() != null) {
            sql.append(" AND md.model_version_id = :modelVersionId");
            parameters.put("modelVersionId", query.getModelVersionId());
        }
        if (hasText(query.getDeploymentScope())) {
            sql.append(" AND md.deployment_scope = :deploymentScope");
            parameters.put("deploymentScope", query.getDeploymentScope().trim());
        }
        if (query.getIsActive() != null) {
            sql.append(" AND md.is_active = :isActive");
            parameters.put("isActive", query.getIsActive());
        }
        if (!count) {
            sql.append(" ORDER BY md.deployed_at DESC, md.deployment_id DESC");
        }
        return new SqlParts(sql.toString(), parameters);
    }

    private long count(SqlParts parts) {
        Query query = entityManager.createNativeQuery(parts.value());
        bind(query, parts.parameters());
        Object result = query.getSingleResult();
        return result instanceof Number number ? number.longValue() : Long.parseLong(String.valueOf(result));
    }

    private void bind(Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private ModelSummaryResponse toModelSummary(Object[] row) {
        return ModelSummaryResponse.builder()
                .modelId(toLong(row[0]))
                .modelName(toStringValue(row[1]))
                .modelType(toStringValue(row[2]))
                .description(toStringValue(row[3]))
                .createdAt(toDateTime(row[4]))
                .build();
    }

    private ModelVersionSummaryResponse toVersionSummary(Object[] row) {
        return ModelVersionSummaryResponse.builder()
                .modelVersionId(toLong(row[0]))
                .modelId(toLong(row[1]))
                .modelName(toStringValue(row[2]))
                .versionName(toStringValue(row[3]))
                .modelCategory(toStringValue(row[4]))
                .modelProfile(toStringValue(row[5]))
                .framework(toStringValue(row[6]))
                .inputSize(toStringValue(row[7]))
                .thresholdDefault(toDecimal(row[8]))
                .accuracy(toDecimal(row[9]))
                .precisionScore(toDecimal(row[10]))
                .recallScore(toDecimal(row[11]))
                .f1Score(toDecimal(row[12]))
                .aurocScore(toDecimal(row[13]))
                .deployStatus(toStringValue(row[14]))
                .isActive(toBoolean(row[15]))
                .validatedAt(toDateTime(row[16]))
                .validatedBy(toLong(row[17]))
                .createdAt(toDateTime(row[18]))
                .build();
    }

    private ModelDeploymentResponse toDeployment(Object[] row) {
        return ModelDeploymentResponse.builder()
                .deploymentId(toLong(row[0]))
                .organizationId(toLong(row[1]))
                .targetId(toLong(row[2]))
                .modelVersionId(toLong(row[3]))
                .modelId(toLong(row[4]))
                .modelName(toStringValue(row[5]))
                .versionName(toStringValue(row[6]))
                .deploymentScope(toStringValue(row[7]))
                .deployStatus(toStringValue(row[8]))
                .isActive(toBoolean(row[9]))
                .deployedAt(toDateTime(row[10]))
                .deployedBy(toLong(row[11]))
                .rollbackFromDeploymentId(toLong(row[12]))
                .reason(toStringValue(row[13]))
                .build();
    }

    private <T> ModelPageResponse<T> toPage(List<T> content, int page, int size, long totalElements) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return ModelPageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String toStringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Number number) return number.intValue() != 0;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bigDecimal) return bigDecimal;
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue());
        return new BigDecimal(String.valueOf(value));
    }

    private LocalDateTime toDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        if (value instanceof LocalDateTime time) return time;
        return Timestamp.valueOf(String.valueOf(value)).toLocalDateTime();
    }

    private record SqlParts(String value, Map<String, Object> parameters) {}
}
