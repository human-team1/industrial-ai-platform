package com.example.factoryguard.adapter.out.persistence.document;

import com.example.factoryguard.application.dto.document.DocumentIndexedChunkResponse;
import com.example.factoryguard.application.dto.document.DocumentIndexJobPollingTarget;
import com.example.factoryguard.application.dto.document.DocumentIndexingTarget;
import com.example.factoryguard.application.dto.document.DocumentUploadResult;
import com.example.factoryguard.application.port.out.document.DocumentIndexPersistencePort;
import com.example.factoryguard.application.port.out.document.LoadVectorIndexPort;
import com.example.factoryguard.application.port.out.document.SaveUploadedDocumentPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.document.model.VectorIndex;
import com.example.factoryguard.domain.document.vo.DocumentIndexJobStatus;
import com.example.factoryguard.domain.document.vo.DocumentStatus;
import com.example.factoryguard.domain.document.vo.DocumentType;
import com.example.factoryguard.domain.document.vo.IndexingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JdbcDocumentPersistenceAdapter implements SaveUploadedDocumentPort, DocumentIndexPersistencePort, LoadVectorIndexPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public DocumentUploadResult save(
            Long organizationId,
            Long ownerUserId,
            String title,
            DocumentType documentType,
            Long originalFileId,
            String fileHash,
            IndexingStatus indexingStatus
    ) {
        Long documentId = insertDocument(organizationId, ownerUserId, title, documentType);
        Long documentVersionId = insertDocumentVersion(
                documentId,
                originalFileId,
                fileHash,
                indexingStatus
        );
        return DocumentUploadResult.builder()
                .documentId(documentId)
                .documentVersionId(documentVersionId)
                .fileId(originalFileId)
                .indexingStatus(indexingStatus.name())
                .build();
    }

    @Override
    public Long createIndexJob(Long documentVersionId, DocumentIndexJobStatus status, LocalDateTime startedAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO document_index_job (document_version_id, job_status, started_at)
                    VALUES (?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, documentVersionId);
            ps.setString(2, status.name());
            ps.setTimestamp(3, Timestamp.valueOf(startedAt));
            return ps;
        }, keyHolder);
        return generatedKey(keyHolder, "문서 인덱싱 작업 저장에 실패했습니다.");
    }

    @Override
    public Optional<DocumentIndexingTarget> findIndexingTarget(Long documentVersionId) {
        List<DocumentIndexingTarget> targets = jdbcTemplate.query("""
                SELECT
                    d.document_id,
                    d.organization_id,
                    d.title,
                    d.document_type,
                    d.category,
                    d.equipment_type,
                    dv.document_version_id,
                    dv.file_id,
                    dv.file_hash,
                    f.object_key,
                    f.file_name,
                    f.mime_type,
                    GROUP_CONCAT(dt.tag_name ORDER BY dt.tag_name SEPARATOR ',') AS tags
                FROM document_version dv
                JOIN document d ON d.document_id = dv.document_id
                JOIN `file` f ON f.file_id = dv.file_id
                LEFT JOIN document_tag dt ON dt.document_id = d.document_id
                WHERE dv.document_version_id = ?
                GROUP BY d.document_id, d.organization_id, d.title, d.document_type, d.category, d.equipment_type,
                         dv.document_version_id, dv.file_id, dv.file_hash, f.object_key, f.file_name, f.mime_type
                """, (rs, rowNum) -> DocumentIndexingTarget.builder()
                .documentId(rs.getLong("document_id"))
                .organizationId(rs.getLong("organization_id"))
                .title(rs.getString("title"))
                .documentType(DocumentType.valueOf(rs.getString("document_type")))
                .category(rs.getString("category"))
                .equipmentType(rs.getString("equipment_type"))
                .documentVersionId(rs.getLong("document_version_id"))
                .fileId(rs.getLong("file_id"))
                .checksum(rs.getString("file_hash"))
                .fileKey(rs.getString("object_key"))
                .fileName(rs.getString("file_name"))
                .mimeType(rs.getString("mime_type"))
                .tags(toTags(rs.getString("tags")))
                .build(), documentVersionId);
        return targets.stream().findFirst();
    }

    @Override
    public Optional<DocumentIndexingTarget> findLatestIndexingTargetByDocumentId(Long documentId) {
        List<Long> versionIds = jdbcTemplate.queryForList("""
                SELECT dv.document_version_id
                FROM document_version dv
                WHERE dv.document_id = ?
                ORDER BY dv.version_no DESC
                LIMIT 1
                """, Long.class, documentId);
        if (versionIds.isEmpty()) {
            return Optional.empty();
        }
        return findIndexingTarget(versionIds.get(0));
    }

    @Override
    public List<DocumentIndexJobPollingTarget> findProcessingJobs(int limit) {
        return jdbcTemplate.query("""
                SELECT dij.job_id, dij.ai_job_id, dv.document_id, dij.document_version_id
                FROM document_index_job dij
                JOIN document_version dv ON dv.document_version_id = dij.document_version_id
                WHERE dij.job_status = ?
                  AND dij.ai_job_id IS NOT NULL
                ORDER BY dij.started_at ASC, dij.job_id ASC
                LIMIT ?
                """, (rs, rowNum) -> DocumentIndexJobPollingTarget.builder()
                .indexJobId(rs.getLong("job_id"))
                .aiJobId(rs.getString("ai_job_id"))
                .documentId(rs.getLong("document_id"))
                .documentVersionId(rs.getLong("document_version_id"))
                .build(), DocumentIndexJobStatus.PROCESSING.name(), Math.max(limit, 1));
    }

    @Override
    public void markProcessing(Long documentId, Long documentVersionId) {
        jdbcTemplate.update("""
                UPDATE document_version
                SET indexing_status = ?, indexed_chunk_count = 0, index_error_message = NULL, indexed_at = NULL
                WHERE document_version_id = ?
                """, IndexingStatus.PROCESSING.name(), documentVersionId);
        jdbcTemplate.update("""
                UPDATE document SET current_status = ? WHERE document_id = ?
                """, DocumentStatus.PROCESSING.name(), documentId);
    }

    @Override
    public void markEnqueued(Long documentId, Long documentVersionId, Long jobId, String aiJobId) {
        jdbcTemplate.update("""
                UPDATE document_version
                SET indexing_status = ?, indexed_chunk_count = 0, index_error_message = NULL, indexed_at = NULL
                WHERE document_version_id = ?
                """, IndexingStatus.PROCESSING.name(), documentVersionId);
        jdbcTemplate.update("""
                UPDATE document_index_job
                SET job_status = ?, ai_job_id = ?, started_at = COALESCE(started_at, ?), error_message = NULL
                WHERE job_id = ?
                """, DocumentIndexJobStatus.PROCESSING.name(), aiJobId, Timestamp.valueOf(LocalDateTime.now()), jobId);
        jdbcTemplate.update("""
                UPDATE document SET current_status = ? WHERE document_id = ?
                """, DocumentStatus.PROCESSING.name(), documentId);
    }

    @Override
    public void replaceChunksAndVectors(
            Long documentVersionId,
            String embeddingModel,
            List<DocumentIndexedChunkResponse> chunks
    ) {
        jdbcTemplate.update("""
                DELETE vi FROM vector_index vi
                JOIN chunk c ON c.chunk_id = vi.chunk_id
                WHERE c.document_version_id = ?
                """, documentVersionId);
        jdbcTemplate.update("DELETE FROM chunk WHERE document_version_id = ?", documentVersionId);
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        for (DocumentIndexedChunkResponse chunk : chunks) {
            Long chunkId = insertChunk(documentVersionId, chunk);
            insertVectorIndex(chunkId, embeddingModel, chunk.getVectorRef());
        }
    }

    @Override
    public void markCompleted(
            Long documentId,
            Long documentVersionId,
            Long jobId,
            int chunkCount,
            LocalDateTime indexedAt
    ) {
        jdbcTemplate.update("""
                UPDATE document_version
                SET indexing_status = ?, indexed_chunk_count = ?, index_error_message = NULL, indexed_at = ?
                WHERE document_version_id = ?
                """, IndexingStatus.COMPLETED.name(), chunkCount, Timestamp.valueOf(indexedAt), documentVersionId);
        jdbcTemplate.update("""
                UPDATE document_index_job
                SET job_status = ?, completed_at = ?, error_message = NULL
                WHERE job_id = ?
                """, DocumentIndexJobStatus.COMPLETED.name(), Timestamp.valueOf(LocalDateTime.now()), jobId);
        jdbcTemplate.update("""
                UPDATE document SET current_status = ? WHERE document_id = ?
                """, DocumentStatus.COMPLETED.name(), documentId);
    }

    @Override
    public void markFailed(Long documentId, Long documentVersionId, Long jobId, String errorMessage) {
        String summary = summarize(errorMessage);
        jdbcTemplate.update("""
                UPDATE document_version
                SET indexing_status = ?, index_error_message = ?
                WHERE document_version_id = ?
                """, IndexingStatus.FAILED.name(), summary, documentVersionId);
        jdbcTemplate.update("""
                UPDATE document_index_job
                SET job_status = ?, completed_at = ?, error_message = ?
                WHERE job_id = ?
                """, DocumentIndexJobStatus.FAILED.name(), Timestamp.valueOf(LocalDateTime.now()), summary, jobId);
        jdbcTemplate.update("""
                UPDATE document SET current_status = ? WHERE document_id = ?
                """, DocumentStatus.FAILED.name(), documentId);
    }

    @Override
    public void recordDeindexFailure(Long documentVersionId, String errorMessage) {
        Long jobId = createIndexJob(documentVersionId, DocumentIndexJobStatus.FAILED, LocalDateTime.now());
        jdbcTemplate.update("""
                UPDATE document_index_job
                SET completed_at = ?, error_message = ?
                WHERE job_id = ?
                """, Timestamp.valueOf(LocalDateTime.now()), summarize(errorMessage), jobId);
    }

    @Override
    public Optional<VectorIndex> findByChunkId(Long chunkId) {
        List<VectorIndex> rows = jdbcTemplate.query("""
                SELECT vector_id, chunk_id, embedding_model, vector_ref, created_at
                FROM vector_index
                WHERE chunk_id = ?
                LIMIT 1
                """, (rs, rowNum) -> VectorIndex.builder()
                .vectorId(rs.getLong("vector_id"))
                .chunkId(rs.getLong("chunk_id"))
                .embeddingModel(rs.getString("embedding_model"))
                .vectorRef(rs.getString("vector_ref"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build(), chunkId);
        return rows.stream().findFirst();
    }

    @Override
    public Optional<VectorIndex> findByVectorRef(String vectorRef) {
        List<VectorIndex> rows = jdbcTemplate.query("""
                SELECT vector_id, chunk_id, embedding_model, vector_ref, created_at
                FROM vector_index
                WHERE vector_ref = ?
                LIMIT 1
                """, (rs, rowNum) -> VectorIndex.builder()
                .vectorId(rs.getLong("vector_id"))
                .chunkId(rs.getLong("chunk_id"))
                .embeddingModel(rs.getString("embedding_model"))
                .vectorRef(rs.getString("vector_ref"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build(), vectorRef);
        return rows.stream().findFirst();
    }

    private Long insertDocument(Long organizationId, Long ownerUserId, String title, DocumentType documentType) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO document (organization_id, owner_user_id, title, document_type, current_status)
                    VALUES (?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, organizationId);
            ps.setLong(2, ownerUserId);
            ps.setString(3, title);
            ps.setString(4, documentType.name());
            ps.setString(5, DocumentStatus.PROCESSING.name());
            return ps;
        }, keyHolder);
        return generatedKey(keyHolder, "문서 메타데이터 저장에 실패했습니다.");
    }

    private Long insertDocumentVersion(
            Long documentId,
            Long originalFileId,
            String fileHash,
            IndexingStatus indexingStatus
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO document_version (
                        document_id, version_no, file_id,
                        file_hash, indexing_status
                    )
                    VALUES (?, 1, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, documentId);
            ps.setLong(2, originalFileId);
            ps.setString(3, fileHash);
            ps.setString(4, indexingStatus.name());
            return ps;
        }, keyHolder);
        return generatedKey(keyHolder, "문서 버전 메타데이터 저장에 실패했습니다.");
    }

    private Long insertChunk(Long documentVersionId, DocumentIndexedChunkResponse chunk) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO chunk (document_version_id, sequence_no, content)
                    VALUES (?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, documentVersionId);
            ps.setInt(2, chunk.getSequenceNo());
            ps.setString(3, chunk.getContent());
            return ps;
        }, keyHolder);
        return generatedKey(keyHolder, "문서 청크 저장에 실패했습니다.");
    }

    private void insertVectorIndex(Long chunkId, String embeddingModel, String vectorRef) {
        jdbcTemplate.update("""
                INSERT INTO vector_index (chunk_id, embedding_model, vector_ref)
                VALUES (?, ?, ?)
                """, chunkId, embeddingModel, vectorRef);
    }

    private Long generatedKey(KeyHolder keyHolder, String message) {
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, message);
        }
        return key.longValue();
    }

    private String summarize(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "문서 인덱싱에 실패했습니다.";
        }
        String normalized = errorMessage.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 500) {
            return normalized;
        }
        return normalized.substring(0, 500);
    }

    private List<String> toTags(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return List.of(raw.split(","));
    }
}
