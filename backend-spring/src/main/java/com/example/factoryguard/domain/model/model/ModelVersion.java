package com.example.factoryguard.domain.model.model;

import com.example.factoryguard.domain.model.vo.ModelDeployStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ModelVersion {

    private final Long modelVersionId;
    private final Long modelId;
    private final Long fileId;
    private final String versionName;
    private final Double accuracy;
    private final Double precisionScore;
    private final Double recallScore;
    private final ModelDeployStatus deployStatus;
    private final Boolean isActive;
    private final LocalDateTime validatedAt;
    private final Long validatedBy;
    private final LocalDateTime createdAt;
}
