package com.example.factoryguard.domain.model.model;

import com.example.factoryguard.domain.model.vo.ModelType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiModel {

    private final Long modelId;
    private final String modelName;
    private final ModelType modelType;
    private final LocalDateTime createdAt;
}
