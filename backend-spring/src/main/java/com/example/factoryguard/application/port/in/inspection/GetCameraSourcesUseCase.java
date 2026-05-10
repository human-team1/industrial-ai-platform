package com.example.factoryguard.application.port.in.inspection;

import com.example.factoryguard.domain.inspection.model.CameraSource;

import java.util.List;

public interface GetCameraSourcesUseCase {
    List<CameraSource> execute(Long organizationId);
}
