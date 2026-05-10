package com.example.factoryguard.domain.result.model;

import com.example.factoryguard.domain.result.vo.ArtifactType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResultArtifact {

    private final Long artifactId;
    private final Long resultId;
    private final ArtifactType artifactType;
    private final Long fileId;
    private final LocalDateTime createdAt;
}
