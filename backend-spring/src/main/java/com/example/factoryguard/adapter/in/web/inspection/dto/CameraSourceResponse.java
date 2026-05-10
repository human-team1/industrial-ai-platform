package com.example.factoryguard.adapter.in.web.inspection.dto;

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
}
