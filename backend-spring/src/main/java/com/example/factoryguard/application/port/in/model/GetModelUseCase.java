package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ModelDetailResponse;

public interface GetModelUseCase {

    ModelDetailResponse getModel(Long modelId);
}
