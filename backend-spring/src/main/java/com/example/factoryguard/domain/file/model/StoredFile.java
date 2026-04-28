package com.example.factoryguard.domain.file.model;

import com.example.factoryguard.domain.file.vo.StorageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StoredFile {

    private final Long fileId;
    private final StorageType storageType;
    private final String bucketName;
    private final String objectKey;
    private final String filePath;
    private final String fileName;
    private final String fileExt;
    private final String mimeType;
    private final Long fileSize;
    private final String checksum;
    private final LocalDateTime createdAt;
    private final Long createdBy;
}
