package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ModelDetailResponse {

    private final Long modelId;
    private final String modelName;
    private final String modelType;
    private final String description;
    private final LocalDateTime createdAt;
}
