package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultModelInfoResponse {

    private final Long modelId;
    private final String modelName;
    private final String versionName;
    private final String modelCategory;
    private final String modelProfile;
}
