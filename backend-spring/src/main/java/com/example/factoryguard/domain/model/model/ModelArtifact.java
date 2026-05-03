package com.example.factoryguard.domain.model.model;

import com.example.factoryguard.domain.model.vo.ModelArtifactType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ModelArtifact {

    private final Long modelArtifactId;
    private final Long modelVersionId;
    private final Long fileId;
    private final ModelArtifactType artifactType;
    private final String checksum;
    private final LocalDateTime createdAt;
}
