package com.example.factoryguard.application.port.in.inspection;

public interface DeleteCameraSourceUseCase {
    void execute(Long organizationId, Long cameraId);
}
