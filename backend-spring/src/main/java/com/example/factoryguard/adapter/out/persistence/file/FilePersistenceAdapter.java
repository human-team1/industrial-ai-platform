package com.example.factoryguard.adapter.out.persistence.file;

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
public class FilePersistenceAdapter implements LoadFilePort {

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
        return Optional.of(StoredFile.builder()
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
                .build());
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
}
