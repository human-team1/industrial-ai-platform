package com.example.factoryguard.application.dto.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreatedModelVersionResponse {

    private final String modelProfile;
    private final Long modelVersionId;
    private final String versionName;
    private final String deployStatus;
    private final Boolean isActive;
    private final Long memoryBankFileId;
    private final Long deploymentId;
}
