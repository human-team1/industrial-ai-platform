package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GenerateModelVersionsFromNormalImagesResponse {

    private final Long modelId;
    private final String modelCategory;
    private final int normalImageCount;
    private final List<CreatedModelVersionResponse> createdVersions;
}
