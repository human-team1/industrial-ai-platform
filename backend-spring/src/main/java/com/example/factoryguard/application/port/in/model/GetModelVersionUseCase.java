package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ModelVersionDetailResponse;

public interface GetModelVersionUseCase {

    ModelVersionDetailResponse getModelVersion(Long versionId);
}
