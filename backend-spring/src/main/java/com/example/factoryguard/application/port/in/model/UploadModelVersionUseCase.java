package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ModelVersionDetailResponse;
import com.example.factoryguard.application.dto.model.UploadModelVersionCommand;

public interface UploadModelVersionUseCase {

    ModelVersionDetailResponse uploadModelVersion(UploadModelVersionCommand command);
}
