package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.application.dto.inspection.UpdateCameraSourceCommand;
import com.example.factoryguard.domain.inspection.model.CameraSource;

public interface UpdateCameraSourceUseCase {
    CameraSource execute(UpdateCameraSourceCommand command);
}
