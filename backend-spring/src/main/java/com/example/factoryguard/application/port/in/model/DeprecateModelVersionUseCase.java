package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ModelVersionDetailResponse;
import com.example.factoryguard.application.dto.model.ModelVersionStatusCommand;

public interface DeprecateModelVersionUseCase {

    ModelVersionDetailResponse deprecateModelVersion(ModelVersionStatusCommand command);
}
