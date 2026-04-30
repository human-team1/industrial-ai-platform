package com.example.factoryguard.adapter.in.web.inspection.dto;

public record CreateCameraSourceRequest(
        String cameraName,
        String streamUrl
) {
}
