package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultArtifactResponse {

    private final Long artifactId;
    private final String artifactType;
    private final Long fileId;
}
