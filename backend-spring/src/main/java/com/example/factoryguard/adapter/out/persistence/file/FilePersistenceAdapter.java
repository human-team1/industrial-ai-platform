package com.example.factoryguard.adapter.out.persistence.file;

import com.example.factoryguard.application.port.out.file.CheckFileAccessPort;
import com.example.factoryguard.application.port.out.file.LoadFilePort;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.file.vo.StorageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FilePersistenceAdapter implements LoadFilePort, CheckFileAccessPort {

    private final EntityManager entityManager;

    @Override
    public Optional<StoredFile> findById(Long fileId) {
        Query query = entityManager.createNativeQuery("""
                SELECT file_id, storage_type, bucket_name, object_key, file_path, file_name,
                       file_ext, mime_type, file_size, checksum, created_at, created_by
                FROM file
                WHERE file_id = :fileId
                """);
        query.setParameter("fileId", fileId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] row = rows.get(0);
        return Optional.of(toStoredFile(row));
    }

    @Override
    public Optional<StoredFile> findByObjectKey(String objectKey) {
        Query query = entityManager.createNativeQuery("""
                SELECT file_id, storage_type, bucket_name, object_key, file_path, file_name,
                       file_ext, mime_type, file_size, checksum, created_at, created_by
                FROM file
                WHERE object_key = :objectKey
                ORDER BY file_id DESC
                LIMIT 1
                """);
        query.setParameter("objectKey", objectKey);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toStoredFile(rows.get(0)));
    }

    @Override
    public boolean canAccess(Long fileId, Long organizationId, boolean siteAdmin) {
        if (siteAdmin) {
            Query query = entityManager.createNativeQuery("""
                    SELECT COUNT(*)
                    FROM file
                    WHERE file_id = :fileId
                    """);
            query.setParameter("fileId", fileId);
            return ((Number) query.getSingleResult()).longValue() > 0;
        }
        if (organizationId == null) {
            return false;
        }
        Query query = entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM file f
                WHERE f.file_id = :fileId
                  AND (
                    EXISTS (
                      SELECT 1
                      FROM inspection_input ii
                      JOIN inspection_run ir ON ir.inspection_id = ii.inspection_id
                      WHERE ii.file_id = f.file_id
                        AND ir.organization_id = :organizationId
                    )
                    OR EXISTS (
                      SELECT 1
                      FROM result_artifact ra
                      JOIN inspection_result r ON r.result_id = ra.result_id
                      JOIN inspection_run ir ON ir.inspection_id = r.inspection_id
                      WHERE ra.file_id = f.file_id
                        AND ir.organization_id = :organizationId
                    )
                    OR EXISTS (
                      SELECT 1
                      FROM image im
                      JOIN inspection_result r ON r.result_id = im.result_id
                      JOIN inspection_run ir ON ir.inspection_id = r.inspection_id
                      WHERE im.file_id = f.file_id
                        AND ir.organization_id = :organizationId
                    )
                    OR EXISTS (
                      SELECT 1
                      FROM document_version dv
                      JOIN document d ON d.document_id = dv.document_id
                      WHERE dv.file_id = f.file_id
                        AND d.organization_id = :organizationId
                        AND d.deleted_at IS NULL
                    )
                    OR EXISTS (
                      SELECT 1
                      FROM report_file rf
                      JOIN report rp ON rp.report_id = rf.report_id
                      WHERE rf.file_id = f.file_id
                        AND rp.organization_id = :organizationId
                    )
                  )
                """);
        query.setParameter("fileId", fileId);
        query.setParameter("organizationId", organizationId);
        return ((Number) query.getSingleResult()).longValue() > 0;
    }

    private StorageType toStorageType(Object value) {
        if (value == null) {
            return StorageType.LOCAL;
        }
        return StorageType.valueOf(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }

    private String toString(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        return ((Timestamp) value).toLocalDateTime();
    }

    private StoredFile toStoredFile(Object[] row) {
        return StoredFile.builder()
                .fileId(toLong(row[0]))
                .storageType(toStorageType(row[1]))
                .bucketName(toString(row[2]))
                .objectKey(toString(row[3]))
                .filePath(toString(row[4]))
                .fileName(toString(row[5]))
                .fileExt(toString(row[6]))
                .mimeType(toString(row[7]))
                .fileSize(toLong(row[8]))
                .checksum(toString(row[9]))
                .createdAt(toLocalDateTime(row[10]))
                .createdBy(toLong(row[11]))
                .build();
    }
}
