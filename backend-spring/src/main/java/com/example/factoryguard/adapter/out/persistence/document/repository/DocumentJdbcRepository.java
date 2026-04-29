package com.example.factoryguard.adapter.out.persistence.document.repository;

import com.example.factoryguard.adapter.out.persistence.document.entity.DocumentEntity;
import com.example.factoryguard.adapter.out.persistence.document.entity.DocumentVersionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class DocumentJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    // 문서 저장
    public DocumentEntity saveDocument(DocumentEntity entity) {
        String sql = """
                INSERT INTO documents (organization_id, owner_user_id, title, document_type, current_status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, entity.getOrganizationId());
            ps.setLong(2, entity.getOwnerUserId());
            ps.setString(3, entity.getTitle());
            ps.setString(4, entity.getDocumentType());
            ps.setString(5, entity.getCurrentStatus());
            ps.setTimestamp(6, Timestamp.valueOf(entity.getCreatedAt()));
            ps.setTimestamp(7, Timestamp.valueOf(entity.getUpdatedAt()));
            return ps;
        }, keyHolder);

        entity.setDocumentId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return entity;
    }

    // 문서 버전 저장
    public DocumentVersionEntity saveDocumentVersion(DocumentVersionEntity entity) {
        String sql = """
                INSERT INTO document_versions (document_id, version_no, file_id, file_hash, indexing_status, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, entity.getDocumentId());
            ps.setInt(2, entity.getVersionNo());
            ps.setLong(3, entity.getFileId());
            ps.setString(4, entity.getFileHash());
            ps.setString(5, entity.getIndexingStatus());
            ps.setTimestamp(6, Timestamp.valueOf(entity.getCreatedAt()));
            return ps;
        }, keyHolder);

        entity.setDocumentVersionId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return entity;
    }
}
