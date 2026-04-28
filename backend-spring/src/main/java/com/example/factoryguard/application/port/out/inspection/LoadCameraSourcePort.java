package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.CameraSource;

import java.util.Optional;

public interface LoadCameraSourcePort {

    Optional<CameraSource> findById(Long cameraId);
}
