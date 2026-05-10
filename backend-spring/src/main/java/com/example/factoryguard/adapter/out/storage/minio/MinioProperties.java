package com.example.factoryguard.adapter.out.storage.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.minio")
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketDocuments;
    private String bucketInspectionArtifacts;
    private String bucketReports;
    private String bucketModels;
    private boolean secure;
    private boolean autoCreateBuckets;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketDocuments() {
        return bucketDocuments;
    }

    public void setBucketDocuments(String bucketDocuments) {
        this.bucketDocuments = bucketDocuments;
    }

    public String getBucketInspectionArtifacts() {
        return bucketInspectionArtifacts;
    }

    public void setBucketInspectionArtifacts(String bucketInspectionArtifacts) {
        this.bucketInspectionArtifacts = bucketInspectionArtifacts;
    }

    public String getBucketReports() {
        return bucketReports;
    }

    public void setBucketReports(String bucketReports) {
        this.bucketReports = bucketReports;
    }

    public String getBucketModels() {
        return bucketModels;
    }

    public void setBucketModels(String bucketModels) {
        this.bucketModels = bucketModels;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isAutoCreateBuckets() {
        return autoCreateBuckets;
    }

    public void setAutoCreateBuckets(boolean autoCreateBuckets) {
        this.autoCreateBuckets = autoCreateBuckets;
    }
}
