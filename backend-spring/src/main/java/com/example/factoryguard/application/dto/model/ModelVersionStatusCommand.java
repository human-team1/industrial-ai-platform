package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModelVersionStatusCommand {

    private final Long versionId;
    private final String reason;
}
