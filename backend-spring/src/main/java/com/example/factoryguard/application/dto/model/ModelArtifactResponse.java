package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ModelArtifactResponse {

    private final Long modelArtifactId;
    private final Long modelVersionId;
    private final Long fileId;
    private final String artifactType;
    private final String fileName;
    private final String objectKey;
    private final String checksum;
    private final LocalDateTime createdAt;
}
