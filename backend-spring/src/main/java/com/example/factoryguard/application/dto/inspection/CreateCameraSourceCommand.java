package com.example.factoryguard.application.dto.inspection;

public record CreateCameraSourceCommand(
        Long userId,
        Long organizationId,
        String cameraName,
        String streamUrl
) {
}
