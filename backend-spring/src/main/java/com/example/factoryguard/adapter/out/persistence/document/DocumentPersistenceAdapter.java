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
            where.append(" AND d.title LIKE :keyword ");
        }
        if (hasText(query.getDocumentType())) {
            where.append(" AND d.document_type = :documentType ");
        }
        if (hasText(query.getIndexingStatus())) {
            where.append(" AND dv.indexing_status = :indexingStatus ");
        }
        if (query.getStartDate() != null) {
            where.append(" AND d.created_at >= :startDate ");
        }
        if (query.getEndDate() != null) {
            where.append(" AND d.created_at < :endDate ");
        }

        String contentSql = """
                SELECT d.document_id, d.title, d.document_type,
                       dv.indexing_status, dv.version_no, d.created_at, d.updated_at, CAST(NULL AS DATETIME) AS last_used_at,
                       f.file_size, f.file_name
                FROM DOCUMENT d
                JOIN (
                    SELECT document_id, MAX(version_no) AS latest_version_no
                    FROM DOCUMENT_VERSION
                    GROUP BY document_id
                ) latest ON latest.document_id = d.document_id
                JOIN DOCUMENT_VERSION dv
                    ON dv.document_id = latest.document_id
                   AND dv.version_no = latest.latest_version_no
                LEFT JOIN FILE f ON f.file_id = dv.file_id
                WHERE
                """ + where + """
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
                    .category(null)
                    .equipmentType(null)
                    .authorName(null)
                    .indexingStatus(toIndexingStatus(row[3]))
                    .versionNo(toInteger(row[4]))
                    .createdAt(toDateTime(row[5]))
                    .updatedAt(toDateTime(row[6]))
                    .lastUsedAt(toDateTime(row[7]))
                    .fileSize(toLong(row[8]))
                    .fileName(toString(row[9]))
                    .build());
        }

        String countSql = """
                SELECT COUNT(*)
                FROM DOCUMENT d
                JOIN (
                    SELECT document_id, MAX(version_no) AS latest_version_no
                    FROM DOCUMENT_VERSION
                    GROUP BY document_id
                ) latest ON latest.document_id = d.document_id
                JOIN DOCUMENT_VERSION dv
                    ON dv.document_id = latest.document_id
                   AND dv.version_no = latest.latest_version_no
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
                FROM DOCUMENT d
                JOIN (
                    SELECT document_id, MAX(version_no) AS latest_version_no
                    FROM DOCUMENT_VERSION
                    GROUP BY document_id
                ) latest ON latest.document_id = d.document_id
                JOIN DOCUMENT_VERSION dv
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
                SELECT d.document_id, d.title, d.document_type,
                       d.owner_user_id, d.current_status,
                       d.created_at AS doc_created_at, d.updated_at AS doc_updated_at,
                       dv.document_version_id, dv.version_no, dv.file_id, f.file_name, f.file_size, f.file_ext,
                       f.mime_type, dv.file_hash, dv.indexing_status, dv.indexed_chunk_count, dv.index_error_message,
                       dv.indexed_at AS version_indexed_at, dv.created_at AS version_created_at
                FROM DOCUMENT d
                JOIN (
                    SELECT document_id, MAX(version_no) AS latest_version_no
                    FROM DOCUMENT_VERSION
                    GROUP BY document_id
                ) latest ON latest.document_id = d.document_id
                JOIN DOCUMENT_VERSION dv
                    ON dv.document_id = latest.document_id
                   AND dv.version_no = latest.latest_version_no
                LEFT JOIN FILE f ON f.file_id = dv.file_id
                WHERE d.document_id = :documentId
                  AND d.deleted_at IS NULL
                """ + (isAdmin ? "" : " AND d.organization_id = :organizationId ");
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
                .category(null)
                .equipmentType(null)
                .description(null)
                .tags(List.of())
                .ownerUserId(toLong(row[3]))
                .authorName(null)
                .currentStatus(toDocumentStatus(row[4]))
                .createdAt(toDateTime(row[5]))
                .updatedAt(toDateTime(row[6]))
                .latestVersion(DocumentVersionDetailResult.builder()
                        .documentVersionId(toLong(row[7]))
                        .versionNo(toInteger(row[8]))
                        .fileId(toLong(row[9]))
                        .fileName(toString(row[10]))
                        .fileSize(toLong(row[11]))
                        .fileExt(toString(row[12]))
                        .mimeType(toString(row[13]))
                        .fileHash(toString(row[14]))
                        .indexingStatus(toIndexingStatus(row[15]))
                        .indexedChunkCount(toInteger(row[16]))
                        .indexErrorMessage(toString(row[17]))
                        .indexedAt(toDateTime(row[18]))
                        .createdAt(toDateTime(row[19]))
                        .build())
                .build());
    }

    @Override
    public Optional<Long> findDocumentOrganizationId(Long documentId) {
        Query q = entityManager.createNativeQuery("""
                SELECT organization_id
                FROM DOCUMENT
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
    public DocumentCreateResult createDocument(Long organizationId, Long ownerUserId, String title, String documentType, String category, String equipmentType, String description, String tags, Long fileId, String authorName) {
        Query insertDocument = entityManager.createNativeQuery("""
                INSERT INTO DOCUMENT (
                    organization_id, owner_user_id, title, document_type, current_status, created_at, updated_at
                ) VALUES (
                    :organizationId, :ownerUserId, :title, :documentType, :currentStatus, :createdAt, :updatedAt
                )
                """);
        LocalDateTime now = LocalDateTime.now();
        insertDocument.setParameter("organizationId", organizationId);
        insertDocument.setParameter("ownerUserId", ownerUserId);
        insertDocument.setParameter("title", title);
        insertDocument.setParameter("documentType", documentType);
        insertDocument.setParameter("currentStatus", DocumentStatus.ACTIVE.name());
        insertDocument.setParameter("createdAt", Timestamp.valueOf(now));
        insertDocument.setParameter("updatedAt", Timestamp.valueOf(now));
        insertDocument.executeUpdate();
        Long documentId = ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();

        Query insertVersion = entityManager.createNativeQuery("""
                INSERT INTO DOCUMENT_VERSION (
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

        createIndexJob(documentVersionId, IndexingStatus.PENDING.name(), null);
        return DocumentCreateResult.builder()
                .documentId(documentId)
                .documentVersionId(documentVersionId)
                .indexingStatus(IndexingStatus.PENDING)
                .build();
    }

    @Override
    public DocumentDetailResult updateMetadata(Long documentId, Long organizationId, boolean isAdmin, String title, String category, String equipmentType, String description, String tags) {
        String sql = """
                UPDATE DOCUMENT
                SET title = :title, updated_at = :updatedAt
                WHERE document_id = :documentId
                  AND deleted_at IS NULL
                """ + (isAdmin ? "" : " AND organization_id = :organizationId ");
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("title", title);
        q.setParameter("updatedAt", Timestamp.valueOf(LocalDateTime.now()));
        q.setParameter("documentId", documentId);
        if (!isAdmin) {
            q.setParameter("organizationId", organizationId);
        }
        int updated = q.executeUpdate();
        if (updated == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서를 찾을 수 없거나 수정 권한이 없습니다.");
        }
        return findDetail(documentId, organizationId, isAdmin)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "문서를 찾을 수 없습니다."));
    }

    @Override
    public DocumentVersionDetailResult createVersion(Long documentId, Long organizationId, boolean isAdmin, Long fileId, String fileHash, String changeReason) {
        String selectSql = """
                SELECT COALESCE(MAX(dv.version_no), 0)
                FROM DOCUMENT d
                JOIN DOCUMENT_VERSION dv ON dv.document_id = d.document_id
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
                INSERT INTO DOCUMENT_VERSION (
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
        entityManager.createNativeQuery("UPDATE DOCUMENT SET updated_at = :updatedAt WHERE document_id = :documentId")
                .setParameter("updatedAt", Timestamp.valueOf(now))
                .setParameter("documentId", documentId)
                .executeUpdate();
        createIndexJob(versionId, IndexingStatus.PENDING.name(), null);

        Object[] row = (Object[]) entityManager.createNativeQuery("""
                SELECT dv.document_version_id, dv.version_no, dv.file_id, f.file_name, f.file_size, f.file_ext,
                       f.mime_type, dv.file_hash, dv.indexing_status, dv.indexed_chunk_count, dv.index_error_message,
                       dv.indexed_at, dv.created_at
                FROM DOCUMENT_VERSION dv
                LEFT JOIN FILE f ON f.file_id = dv.file_id
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
                UPDATE DOCUMENT
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
                INSERT INTO DOCUMENT_INDEX_JOB (
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
