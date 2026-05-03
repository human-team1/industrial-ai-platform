package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ModelVersionDetailResponse {

    private final ModelVersionSummaryResponse version;
    private final List<ModelArtifactResponse> artifacts;
}
