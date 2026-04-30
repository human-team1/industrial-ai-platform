package com.example.factoryguard.domain.inspection.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CameraSource {

    private final Long cameraId;
    private final Long organizationId;
    private final Long userId;
    private final String cameraName;
    private final String streamUrl;
    private final CameraStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
