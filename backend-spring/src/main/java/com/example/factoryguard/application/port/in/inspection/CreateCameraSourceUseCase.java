package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.CreateCameraSourceCommand;
import com.example.factoryguard.domain.inspection.model.CameraSource;

public interface CreateCameraSourceUseCase {
    CameraSource execute(CreateCameraSourceCommand command);
}
