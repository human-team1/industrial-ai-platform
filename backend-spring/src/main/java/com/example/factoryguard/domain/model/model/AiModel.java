package com.example.factoryguard.domain.model.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiModel {

    private final Long modelId;
    private final String modelName;
    private final String modelType;
    private final String description;
    private final LocalDateTime createdAt;
}
