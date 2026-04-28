package com.example.factoryguard.application.dto.result;

import com.example.factoryguard.domain.result.vo.ArtifactType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultArtifactResult {

    private final Long artifactId;
    private final Long resultId;
    private final ArtifactType artifactType;
    private final Long fileId;
}
