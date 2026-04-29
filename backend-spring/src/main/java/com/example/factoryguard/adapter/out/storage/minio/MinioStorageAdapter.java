package com.example.factoryguard.adapter.out.storage.minio;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MinioStorageAdapter {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public MinioStorageAdapter(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    public List<String> configuredBuckets() {
        return List.of(
                properties.getBucketDocuments(),
                properties.getBucketInspectionArtifacts(),
                properties.getBucketReports(),
                properties.getBucketModels()
        );
    }

    public void ensureConfiguredBuckets() {
        for (String bucketName : configuredBuckets()) {
            ensureBucket(bucketName);
        }
    }

    public void ensureBucket(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists && properties.isAutoCreateBuckets()) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception exception) {
            throw new IllegalStateException("MinIO bucket check failed: " + bucketName, exception);
        }
    }

    public StoredObjectMetadata upload(
            String bucketName,
            String objectKey,
            InputStream content,
            long fileSize,
            String fileName,
            String mimeType,
            String checksum
    ) {
        ensureBucket(bucketName);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(content, fileSize, -1)
                    .contentType(mimeType)
                    .build());
            return new StoredObjectMetadata(bucketName, objectKey, fileName, mimeType, fileSize, checksum);
        } catch (Exception exception) {
            throw new IllegalStateException("MinIO upload failed: " + bucketName + "/" + objectKey, exception);
        }
    }

    public String createPresignedGetUrl(String bucketName, String objectKey, Duration expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry((int) expiry.toSeconds(), TimeUnit.SECONDS)
                    .build());
        } catch (Exception exception) {
            throw new IllegalStateException("MinIO presigned URL creation failed: " + bucketName + "/" + objectKey, exception);
        }
    }

    public byte[] download(String bucketName, String objectKey) {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .build())) {
            return stream.readAllBytes();
        } catch (Exception exception) {
            throw new IllegalStateException("MinIO download failed: " + bucketName + "/" + objectKey, exception);
        }
    }

    public boolean canAccessDocumentsBucket() {
        String bucketName = properties.getBucketDocuments();
        if (!StringUtils.hasText(bucketName)) {
            return false;
        }
        try {
            ensureBucket(bucketName);
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object("__healthcheck__")
                    .build());
            return true;
        } catch (io.minio.errors.ErrorResponseException exception) {
            return "NoSuchKey".equals(exception.errorResponse().code());
        } catch (Exception exception) {
            return false;
        }
    }
}
