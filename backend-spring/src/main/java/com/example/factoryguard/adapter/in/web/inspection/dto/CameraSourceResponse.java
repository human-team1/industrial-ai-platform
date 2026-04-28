package com.example.factoryguard.adapter.in.web.inspection.dto;

import com.example.factoryguard.domain.inspection.model.CameraSource;
import com.example.factoryguard.domain.inspection.model.CameraStatus;

import java.time.LocalDateTime;

public record CameraSourceResponse(
        Long cameraId,
        Long organizationId,
        Long userId,
        String cameraName,
        String streamUrl,
        CameraStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CameraSourceResponse from(CameraSource c) {
        return new CameraSourceResponse(
                c.getCameraId(),
                c.getOrganizationId(),
                c.getUserId(),
                c.getCameraName(),
                c.getStreamUrl(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
