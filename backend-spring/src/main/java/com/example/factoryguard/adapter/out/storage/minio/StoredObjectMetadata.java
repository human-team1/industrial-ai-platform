package com.example.factoryguard.adapter.out.storage.minio;

public record StoredObjectMetadata(
        String bucketName,
        String objectKey,
        String fileName,
        String mimeType,
        long fileSize,
        String checksum
) {
}
