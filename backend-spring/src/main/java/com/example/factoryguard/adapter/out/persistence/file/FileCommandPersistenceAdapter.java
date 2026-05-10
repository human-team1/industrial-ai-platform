package com.example.factoryguard.adapter.out.persistence.file;

import com.example.factoryguard.application.port.out.file.PersistUploadedFilePort;
import com.example.factoryguard.domain.file.model.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class FileCommandPersistenceAdapter implements PersistUploadedFilePort {

    private final EntityManager entityManager;

    @Transactional
    @Override
    public StoredFile save(StoredFile file) {
        Query query = entityManager.createNativeQuery("""
                INSERT INTO file (
                    storage_type, bucket_name, object_key, file_path, file_name, file_ext,
                    mime_type, file_size, checksum, created_at, created_by
                ) VALUES (
                    :storageType, :bucketName, :objectKey, :filePath, :fileName, :fileExt,
                    :mimeType, :fileSize, :checksum, :createdAt, :createdBy
                )
                """);
        query.setParameter("storageType", file.getStorageType().name());
        query.setParameter("bucketName", file.getBucketName());
        query.setParameter("objectKey", file.getObjectKey());
        query.setParameter("filePath", file.getFilePath());
        query.setParameter("fileName", file.getFileName());
        query.setParameter("fileExt", file.getFileExt());
        query.setParameter("mimeType", file.getMimeType());
        query.setParameter("fileSize", file.getFileSize());
        query.setParameter("checksum", file.getChecksum());
        query.setParameter("createdAt", Timestamp.valueOf(file.getCreatedAt() != null ? file.getCreatedAt() : LocalDateTime.now()));
        query.setParameter("createdBy", file.getCreatedBy());
        query.executeUpdate();

        Number id = (Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult();
        return StoredFile.builder()
                .fileId(id.longValue())
                .storageType(file.getStorageType())
                .bucketName(file.getBucketName())
                .objectKey(file.getObjectKey())
                .filePath(file.getFilePath())
                .fileName(file.getFileName())
                .fileExt(file.getFileExt())
                .mimeType(file.getMimeType())
                .fileSize(file.getFileSize())
                .checksum(file.getChecksum())
                .createdAt(file.getCreatedAt() != null ? file.getCreatedAt() : LocalDateTime.now())
                .createdBy(file.getCreatedBy())
                .build();
    }
}
