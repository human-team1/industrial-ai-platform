package com.example.factoryguard.adapter.out.persistence.document;

import com.example.factoryguard.application.dto.document.DocumentCreateResult;
import com.example.factoryguard.application.dto.document.DocumentDetailResult;
import com.example.factoryguard.application.dto.document.DocumentListItem;
import com.example.factoryguard.application.dto.document.DocumentListPageResult;
import com.example.factoryguard.application.dto.document.DocumentSearchQuery;
import com.example.factoryguard.application.dto.document.DocumentSummaryResult;
import com.example.factoryguard.application.dto.document.DocumentVersionDetailResult;
import com.example.factoryguard.application.port.out.document.DocumentCrudPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.document.vo.DocumentStatus;
import com.example.factoryguard.domain.document.vo.IndexingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements DocumentCrudPort {

    private final EntityManager entityManager;

    @Override
    public DocumentListPageResult findDocuments(DocumentSearchQuery query, Long organizationId, boolean isAdmin) {
        String baseWhere = " d.deleted_at IS NULL ";
        StringBuilder where = new StringBuilder(baseWhere);
        if (!isAdmin) {
            where.append(" AND d.organization_id = :organizationId ");
        }
        if (hasText(query.getKeyword())) {
            where.append(" AND (d.title LIKE :keyword OR d.description LIKE :keyword OR EXISTS (SELECT 1 FROM document_tag dt WHERE dt.document_id = d.document_id AND dt.tag_name LIKE :keyword)) ");
        }
        if (hasText(query.getDocumentType())) {
            where.append(" AND d.document_type = :documentType ");
        }
        if (hasText(query.getIndexingStatus())) {
            where.append(" AND dv.indexing_status = :indexingStatus ");
        }
        if (hasText(query.getCategory())) {
            where.append(" AND d.category = :category ");
        }
        if (hasText(query.getEquipmentType())) {
            where.append(" AND d.equipment_type = :equipmentType ");
        }
        if (query.getStartDate() != null) {
            where.append(" AND d.created_at >= :startDate ");
        }
        if (query.getEndDate() != null) {
            where.append(" AND d.created_at < :endDate ");
        }

        String contentSql = """
                SELECT d.document_id, d.title, d.document_type, d.category, d.equipment_type,
                       GROUP_CONCAT(dt.tag_name ORDER BY dt.tag_name SEPARATOR ',') AS tags,
                       dv.indexing_status, dv.version_no, d.created_at, d.updated_at, CAST(NULL AS DATETIME) AS last_used_at,
                       f.file_size, f.file_name
                FROM document d
                JOIN (
                    SELECT document_id, MAX(version_no) AS latest_version_no
                    FROM document_version
                    GROUP BY document_id
                ) latest ON latest.document_id = d.document_id
                JOIN document_version dv
                    ON dv.document_id = latest.document_id
                   AND dv.version_no = latest.latest_version_no
                LEFT JOIN document_tag dt ON dt.document_id = d.document_id
                LEFT JOIN file f ON f.file_id = dv.file_id
                WHERE
                """ + where + """
                GROUP BY d.document_id, d.title, d.document_type, d.category, d.equipment_type, dv.indexing_status, dv.version_no, d.created_at, d.updated_at, f.file_size, f.file_name
                ORDER BY COALESCE(d.updated_at, d.created_at) DESC
                LIMIT :limit OFFSET :offset
                """;
        Query contentQuery = entityManager.createNativeQuery(contentSql);
        bindQueryParams(contentQuery, query, organizationId, isAdmin);
        contentQuery.setParameter("limit", query.getSize());
        contentQuery.setParameter("offset", query.getPage() * query.getSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = contentQuery.getResultList();
        List<DocumentListItem> content = new ArrayList<>();
        for (Object[] row : rows) {
            content.add(DocumentListItem.builder()
                    .documentId(toLong(row[0]))
                    .title(toString(row[1]))
                    .documentType(toString(row[2]))
                    .category(toString(row[3]))
                    .equipmentType(toString(row[4]))
                    .tags(toTags(row[5]))
                    .authorName(null)
                    .indexingStatus(toIndexingStatus(row[6]))
                    .versionNo(toInteger(row[7]))
                    .createdAt(toDateTime(row[8]))
                    .updatedAt(toDateTime(row[9]))
                    .lastUsedAt(toDateTime(row[10]))
                    .fileSize(toLong(row[11]))
                    .fileName(toString(row[12]))
                    .build());
        }

        String countSql = """
                SELECT COUNT(DISTINCT d.document_id)
                FROM document d
                JOIN (
                    SELECT document_id, MAX(version_no) AS latest_version_no
                    FROM document_version
                    GROUP BY document_id
                ) latest ON latest.document_id = d.document_id
                JOIN document_version dv
                    ON dv.document_id = latest.document_id
                   AND dv.version_no = latest.latest_version_no
                LEFT JOIN document_tag dt ON dt.document_id = d.document_id
                WHERE
                """ + where;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bindQueryParams(countQuery, query, organizationId, isAdmin);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / query.getSize());

        return DocumentListPageResult.builder()
                .content(content)
                .page(query.getPage())
                .size(query.getSize())
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public DocumentSummaryResult summarize(Long organizationId, boolean isAdmin) {
        String sql = """
                SELECT
                    COUNT(*) AS total_count,
                    SUM(CASE WHEN d.document_type = 'PDF' THEN 1 ELSE 0 END) AS pdf_count,
                    SUM(CASE WHEN d.document_type = 'DOCX' THEN 1 ELSE 0 END) AS docx_count,
                    SUM(CASE WHEN dv.indexing_status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_count,
                    SUM(CASE WHEN dv.indexing_status = 'PROCESSING' THEN 1 ELSE 0 END) AS processing_count,
                    SUM(CASE WHEN dv.indexing_status = 'FAILED' THEN 1 ELSE 0 END) AS failed_count
                FROM document d
                JOIN (
                    SELECT document_id, MAX(version_no) AS latest_version_no
                    FROM document_version
                    GROUP BY document_id
                ) latest ON latest.document_id = d.document_id
                JOIN document_version dv
                    ON dv.document_id = latest.document_id
                   AND dv.version_no = latest.latest_version_no
                WHERE d.deleted_at IS NULL
                """ + (isAdmin ? "" : " AND d.organization_id = :organizationId ");
        Query q = entityManager.createNativeQuery(sql);
        if (!isAdmin) {
            q.setParameter("organizationId", organizationId);
        }
        Object[] row = (Object[]) q.getSingleResult();
        return DocumentSummaryResult.builder()
                .totalCount(toLongOrZero(row[0]))
                .pdfCount(toLongOrZero(row[1]))
                .docxCount(toLongOrZero(row[2]))
                .completedCount(toLongOrZero(row[3]))
                .processingCount(toLongOrZero(row[4]))
                .failedCount(toLongOrZero(row[5]))
                .build();
    }

    @Override
    public Optional<DocumentDetailResult> findDetail(Long documentId, Long organizationId, boolean isAdmin) {
        String sql = """
                SELECT d.document_id, d.title, d.document_type, d.category, d.equipment_type, d.description,
                       GROUP_CONCAT(dt.tag_name ORDER BY dt.tag_name SEPARATOR ',') AS tags,
                       d.owner_user_id, d.current_status,
                       d.created_at AS doc_created_at, d.updated_at AS doc_updated_at,
                       dv.document_version_id, dv.version_no, dv.file_id, f.file_name, f.file_size, f.file_ext,
                       f.mime_type, dv.file_hash, dv.indexing_status, dv.indexed_chunk_count, dv.index_error_message,
                       dv.indexed_at AS version_indexed_at, dv.created_at AS version_created_at
                FROM document d
                JOIN (
                    SELECT document_id, MAX(version_no) AS latest_version_no
                    FROM document_version
                    GROUP BY document_id
                ) latest ON latest.document_id = d.document_id
                JOIN document_version dv
                    ON dv.document_id = latest.document_id
                   AND dv.version_no = latest.latest_version_no
                LEFT JOIN document_tag dt ON dt.document_id = d.document_id
                LEFT JOIN file f ON f.file_id = dv.file_id
                WHERE d.document_id = :documentId
                  AND d.deleted_at IS NULL
                """ + (isAdmin ? "" : " AND d.organization_id = :organizationId ") + """
                GROUP BY d.document_id, d.title, d.document_type, d.category, d.equipment_type, d.description,
                         d.owner_user_id, d.current_status, d.created_at, d.updated_at,
                         dv.document_version_id, dv.version_no, dv.file_id, f.file_name, f.file_size, f.file_ext,
                         f.mime_type, dv.file_hash, dv.indexing_status, dv.indexed_chunk_count, dv.index_error_message,
                         dv.indexed_at, dv.created_at
                """;
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("documentId", documentId);
        if (!isAdmin) {
            q.setParameter("organizationId", organizationId);
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] row = rows.get(0);
        return Optional.of(DocumentDetailResult.builder()
                .documentId(toLong(row[0]))
                .title(toString(row[1]))
                .documentType(toString(row[2]))
                .category(toString(row[3]))
                .equipmentType(toString(row[4]))
                .description(toString(row[5]))
                .tags(toTags(row[6]))
                .ownerUserId(toLong(row[7]))
                .authorName(null)
                .currentStatus(toDocumentStatus(row[8]))
                .createdAt(toDateTime(row[9]))
                .updatedAt(toDateTime(row[10]))
                .latestVersion(DocumentVersionDetailResult.builder()
                        .documentVersionId(toLong(row[11]))
                        .versionNo(toInteger(row[12]))
                        .fileId(toLong(row[13]))
                        .fileName(toString(row[14]))
                        .fileSize(toLong(row[15]))
                        .fileExt(toString(row[16]))
                        .mimeType(toString(row[17]))
                        .fileHash(toString(row[18]))
                        .indexingStatus(toIndexingStatus(row[19]))
                        .indexedChunkCount(toInteger(row[20]))
                        .indexErrorMessage(toString(row[21]))
                        .indexedAt(toDateTime(row[22]))
                        .createdAt(toDateTime(row[23]))
                        .build())
                .build());
    }

    @Override
    public Optional<Long> findDocumentOrganizationId(Long documentId) {
        Query q = entityManager.createNativeQuery("""
                SELECT organization_id
                FROM document
                WHERE document_id = :documentId
                  AND deleted_at IS NULL
                """);
        q.setParameter("documentId", documentId);
        @SuppressWarnings("unchecked")
        List<Object> rows = q.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(((Number) rows.get(0)).longValue());
    }

    @Override
    public DocumentCreateResult createDocument(Long organizationId, Long ownerUserId, String title, String documentType, String category, String equipmentType, String description, List<String> tags, Long fileId, String authorName) {
        Query insertDocument = entityManager.createNativeQuery("""
                INSERT INTO document (
                    organization_id, owner_user_id, title, document_type, category, equipment_type, description, current_status, created_at, updated_at
                ) VALUES (
                    :organizationId, :ownerUserId, :title, :documentType, :category, :equipmentType, :description, :currentStatus, :createdAt, :updatedAt
                )
                """);
        LocalDateTime now = LocalDateTime.now();
        insertDocument.setParameter("organizationId", organizationId);
        insertDocument.setParameter("ownerUserId", ownerUserId);
        insertDocument.setParameter("title", title);
        insertDocument.setParameter("documentType", documentType);
        insertDocument.setParameter("category", category);
        insertDocument.setParameter("equipmentType", equipmentType);
        insertDocument.setParameter("description", description);
        insertDocument.setParameter("currentStatus", DocumentStatus.ACTIVE.name());
        insertDocument.setParameter("createdAt", Timestamp.valueOf(now));
        insertDocument.setParameter("updatedAt", Timestamp.valueOf(now));
        insertDocument.executeUpdate();
        Long documentId = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();

        Query insertVersion = entityManager.createNativeQuery("""
                INSERT INTO document_version (
                    document_id, version_no, file_id, file_hash, indexing_status, indexed_chunk_count,
                    index_error_message, indexed_at, created_at
                ) VALUES (
                    :documentId, 1, :fileId, NULL, :indexingStatus, 0, NULL, NULL, :createdAt
                )
                """);
        insertVersion.setParameter("documentId", documentId);
        insertVersion.setParameter("fileId", fileId);
        insertVersion.setParameter("indexingStatus", IndexingStatus.PENDING.name());
        insertVersion.setParameter("createdAt", Timestamp.valueOf(now));
        insertVersion.executeUpdate();
        Long documentVersionId = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();

        replaceDocumentTags(documentId, tags);
        createIndexJob(documentVersionId, IndexingStatus.PENDING.name(), null);
        return DocumentCreateResult.builder()
                .documentId(documentId)
                .documentVersionId(documentVersionId)
                .indexingStatus(IndexingStatus.PENDING)
                .build();
    }

    @Override
    public DocumentDetailResult updateMetadata(Long documentId, Long organizationId, boolean isAdmin, String title, String category, String equipmentType, String description, List<String> tags) {
        String sql = """
                UPDATE document
                SET title = :title,
                    category = :category,
                    equipment_type = :equipmentType,
                    description = :description,
                    updated_at = :updatedAt
                WHERE document_id = :documentId
                  AND deleted_at IS NULL
                """ + (isAdmin ? "" : " AND organization_id = :organizationId ");
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("title", title);
        q.setParameter("category", category);
        q.setParameter("equipmentType", equipmentType);
        q.setParameter("description", description);
        q.setParameter("updatedAt", Timestamp.valueOf(LocalDateTime.now()));
        q.setParameter("documentId", documentId);
        if (!isAdmin) {
            q.setParameter("organizationId", organizationId);
        }
        int updated = q.executeUpdate();
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서를 찾을 수 없거나 수정 권한이 없습니다.");
        }
        replaceDocumentTags(documentId, tags);
        return findDetail(documentId, organizationId, isAdmin)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서를 찾을 수 없습니다."));
    }

    @Override
    public DocumentVersionDetailResult createVersion(Long documentId, Long organizationId, boolean isAdmin, Long fileId, String fileHash, String changeReason) {
        String selectSql = """
                SELECT COALESCE(MAX(dv.version_no), 0)
                FROM document d
                JOIN document_version dv ON dv.document_id = d.document_id
                WHERE d.document_id = :documentId
                  AND d.deleted_at IS NULL
                """ + (isAdmin ? "" : " AND d.organization_id = :organizationId ");
        Query select = entityManager.createNativeQuery(selectSql);
        select.setParameter("documentId", documentId);
        if (!isAdmin) {
            select.setParameter("organizationId", organizationId);
        }
        Number currentVersionNo = (Number) select.getSingleResult();
        if (currentVersionNo == null || currentVersionNo.intValue() == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서를 찾을 수 없거나 수정 권한이 없습니다.");
        }
        int nextVersion = currentVersionNo.intValue() + 1;
        LocalDateTime now = LocalDateTime.now();
        Query insertVersion = entityManager.createNativeQuery("""
                INSERT INTO document_version (
                    document_id, version_no, file_id, file_hash, indexing_status, indexed_chunk_count,
                    index_error_message, indexed_at, created_at
                ) VALUES (
                    :documentId, :versionNo, :fileId, :fileHash, :indexingStatus, 0, NULL, NULL, :createdAt
                )
                """);
        insertVersion.setParameter("documentId", documentId);
        insertVersion.setParameter("versionNo", nextVersion);
        insertVersion.setParameter("fileId", fileId);
        insertVersion.setParameter("fileHash", fileHash);
        insertVersion.setParameter("indexingStatus", IndexingStatus.PENDING.name());
        insertVersion.setParameter("createdAt", Timestamp.valueOf(now));
        insertVersion.executeUpdate();
        Long versionId = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();
        entityManager.createNativeQuery("UPDATE document SET updated_at = :updatedAt WHERE document_id = :documentId")
                .setParameter("updatedAt", Timestamp.valueOf(now))
                .setParameter("documentId", documentId)
                .executeUpdate();
        createIndexJob(versionId, IndexingStatus.PENDING.name(), null);

        Object[] row = (Object[]) entityManager.createNativeQuery("""
                SELECT dv.document_version_id, dv.version_no, dv.file_id, f.file_name, f.file_size, f.file_ext,
                       f.mime_type, dv.file_hash, dv.indexing_status, dv.indexed_chunk_count, dv.index_error_message,
                       dv.indexed_at, dv.created_at
                FROM document_version dv
                LEFT JOIN file f ON f.file_id = dv.file_id
                WHERE dv.document_version_id = :versionId
                """)
                .setParameter("versionId", versionId)
                .getSingleResult();
        return DocumentVersionDetailResult.builder()
                .documentVersionId(toLong(row[0]))
                .versionNo(toInteger(row[1]))
                .fileId(toLong(row[2]))
                .fileName(toString(row[3]))
                .fileSize(toLong(row[4]))
                .fileExt(toString(row[5]))
                .mimeType(toString(row[6]))
                .fileHash(toString(row[7]))
                .indexingStatus(toIndexingStatus(row[8]))
                .indexedChunkCount(toInteger(row[9]))
                .indexErrorMessage(toString(row[10]))
                .indexedAt(toDateTime(row[11]))
                .createdAt(toDateTime(row[12]))
                .build();
    }

    @Override
    public boolean softDelete(Long documentId, Long organizationId, boolean isAdmin) {
        String sql = """
                UPDATE document
                SET deleted_at = :deletedAt, current_status = :deletedStatus, updated_at = :updatedAt
                WHERE document_id = :documentId
                  AND deleted_at IS NULL
                """ + (isAdmin ? "" : " AND organization_id = :organizationId ");
        Query q = entityManager.createNativeQuery(sql);
        LocalDateTime now = LocalDateTime.now();
        q.setParameter("deletedAt", Timestamp.valueOf(now));
        q.setParameter("deletedStatus", DocumentStatus.DELETED.name());
        q.setParameter("updatedAt", Timestamp.valueOf(now));
        q.setParameter("documentId", documentId);
        if (!isAdmin) {
            q.setParameter("organizationId", organizationId);
        }
        int updated = q.executeUpdate();
        return updated > 0;
    }

    private void createIndexJob(Long documentVersionId, String status, String errorMessage) {
        entityManager.createNativeQuery("""
                INSERT INTO document_index_job (
                    document_version_id, job_status, error_message, started_at, completed_at
                ) VALUES (
                    :documentVersionId, :jobStatus, :errorMessage, NULL, NULL
                )
                """)
                .setParameter("documentVersionId", documentVersionId)
                .setParameter("jobStatus", status)
                .setParameter("errorMessage", errorMessage)
                .executeUpdate();
    }

    private void replaceDocumentTags(Long documentId, List<String> tags) {
        entityManager.createNativeQuery("DELETE FROM document_tag WHERE document_id = :documentId")
                .setParameter("documentId", documentId)
                .executeUpdate();
        if (tags == null || tags.isEmpty()) {
            return;
        }
        Query insert = entityManager.createNativeQuery("""
                INSERT INTO document_tag (document_id, tag_name, created_at)
                VALUES (:documentId, :tagName, :createdAt)
                """);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        for (String tag : tags) {
            insert.setParameter("documentId", documentId);
            insert.setParameter("tagName", tag);
            insert.setParameter("createdAt", now);
            insert.executeUpdate();
        }
    }

    private void bindQueryParams(Query query, DocumentSearchQuery request, Long organizationId, boolean isAdmin) {
        if (!isAdmin) {
            query.setParameter("organizationId", organizationId);
        }
        if (hasText(request.getKeyword())) {
            query.setParameter("keyword", "%" + request.getKeyword() + "%");
        }
        if (hasText(request.getDocumentType())) {
            query.setParameter("documentType", request.getDocumentType());
        }
        if (hasText(request.getIndexingStatus())) {
            query.setParameter("indexingStatus", request.getIndexingStatus());
        }
        if (hasText(request.getCategory())) {
            query.setParameter("category", request.getCategory());
        }
        if (hasText(request.getEquipmentType())) {
            query.setParameter("equipmentType", request.getEquipmentType());
        }
        if (request.getStartDate() != null) {
            query.setParameter("startDate", Timestamp.valueOf(request.getStartDate().atStartOfDay()));
        }
        if (request.getEndDate() != null) {
            query.setParameter("endDate", Timestamp.valueOf(request.getEndDate().plusDays(1).atStartOfDay()));
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private long toLongOrZero(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private List<String> toTags(Object value) {
        if (value == null) {
            return List.of();
        }
        String raw = String.valueOf(value);
        if (raw.isBlank()) {
            return List.of();
        }
        return List.of(raw.split(","));
    }

    private LocalDateTime toDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        return ((Timestamp) value).toLocalDateTime();
    }

    private IndexingStatus toIndexingStatus(Object value) {
        if (value == null) {
            return IndexingStatus.PENDING;
        }
        return IndexingStatus.valueOf(String.valueOf(value));
    }

    private DocumentStatus toDocumentStatus(Object value) {
        if (value == null) {
            return DocumentStatus.ACTIVE;
        }
        return DocumentStatus.valueOf(String.valueOf(value));
    }

}
