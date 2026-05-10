package com.example.factoryguard.adapter.out.storage.minio;

import com.example.factoryguard.application.port.out.file.StoreDocumentFilePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.file.vo.StorageType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DocumentFileStorageAdapter implements StoreDocumentFilePort {

    private final MinioStorageAdapter minioStorageAdapter;
    private final MinioProperties minioProperties;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public StoredFile store(
            byte[] content,
            String originalFileName,
            String mimeType,
            String checksum,
            Long createdBy
    ) {
        String bucket = minioProperties.getBucketDocuments();
        String objectKey = buildObjectKey(originalFileName);
        try {
            minioStorageAdapter.upload(
                    bucket,
                    objectKey,
                    new ByteArrayInputStream(content),
                    content.length,
                    originalFileName,
                    mimeType,
                    checksum
            );
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "문서 파일 저장에 실패했습니다.");
        }

        StoredFile storedFile = StoredFile.builder()
                .storageType(StorageType.MINIO)
                .bucketName(bucket)
                .objectKey(objectKey)
                .filePath(null)
                .fileName(originalFileName)
                .fileExt(fileExtension(originalFileName))
                .mimeType(mimeType)
                .fileSize((long) content.length)
                .checksum(checksum)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
        return persist(storedFile);
    }

    private StoredFile persist(StoredFile file) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO `FILE` (
                        storage_type, bucket_name, object_key, file_path, file_name,
                        file_ext, mime_type, file_size, checksum, created_by
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, file.getStorageType().name());
            ps.setString(2, file.getBucketName());
            ps.setString(3, file.getObjectKey());
            ps.setString(4, file.getFilePath());
            ps.setString(5, file.getFileName());
            ps.setString(6, file.getFileExt());
            ps.setString(7, file.getMimeType());
            ps.setLong(8, file.getFileSize());
            ps.setString(9, file.getChecksum());
            ps.setObject(10, file.getCreatedBy());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "파일 메타데이터 저장에 실패했습니다.");
        }

        return StoredFile.builder()
                .fileId(key.longValue())
                .storageType(file.getStorageType())
                .bucketName(file.getBucketName())
                .objectKey(file.getObjectKey())
                .filePath(file.getFilePath())
                .fileName(file.getFileName())
                .fileExt(file.getFileExt())
                .mimeType(file.getMimeType())
                .fileSize(file.getFileSize())
                .checksum(file.getChecksum())
                .createdAt(file.getCreatedAt())
                .createdBy(file.getCreatedBy())
                .build();
    }

    private String buildObjectKey(String fileName) {
        String ext = fileExtension(fileName);
        String suffix = ext.isBlank() ? "" : "." + ext;
        return "documents/" + UUID.randomUUID() + suffix;
    }

    private String fileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
