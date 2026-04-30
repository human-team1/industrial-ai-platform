package com.example.factoryguard.application.dto.inspection;

import com.example.factoryguard.domain.inspection.model.CameraStatus;

public record UpdateCameraSourceCommand(
        Long cameraId,
        Long organizationId,
        String cameraName,
        String streamUrl,
        CameraStatus status
) {
}
