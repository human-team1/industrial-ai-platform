package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ModelVersionDetailResponse;
import com.example.factoryguard.application.dto.model.ModelVersionStatusCommand;

public interface DeleteModelVersionUseCase {
    ModelVersionDetailResponse deleteModelVersion(ModelVersionStatusCommand command);
}
