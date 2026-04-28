package com.example.factoryguard.domain.inspection.model;

import com.example.factoryguard.domain.inspection.vo.CameraSourceStatus;
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
    private final CameraSourceStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
