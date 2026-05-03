package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.CreateModelCommand;
import com.example.factoryguard.application.dto.model.ModelDetailResponse;

public interface CreateModelUseCase {

    ModelDetailResponse createModel(CreateModelCommand command);
}
