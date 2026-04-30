package com.example.factoryguard.application.port.out.inspection;

import com.example.factoryguard.domain.inspection.model.CameraSource;

public interface SaveCameraSourcePort {
    CameraSource save(CameraSource camera);
}
