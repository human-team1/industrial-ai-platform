package com.example.factoryguard.domain.inspection.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InspectionInput {

    private final Long inspectionInputId;
    private final Long inspectionId;
    private final Long fileId;
    private final Long cameraId;
    private final String streamUrl;
    private final InputSourceType sourceType;
    private final String sourceName;
    private final String mimeType;
    private final Integer durationSec;
    private final Integer frameCount;
    private final LocalDateTime createdAt;
}
