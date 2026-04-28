package com.example.factoryguard.adapter.in.web.inspection.dto;

import com.example.factoryguard.domain.inspection.model.CameraStatus;

public record UpdateCameraSourceRequest(
        String cameraName,
        String streamUrl,
        CameraStatus status
) {
}
