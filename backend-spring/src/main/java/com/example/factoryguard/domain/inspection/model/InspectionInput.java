package com.example.factoryguard.domain.inspection.model;

import com.example.factoryguard.domain.inspection.vo.RoiMode;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
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
    private final RoiMode roiMode;
    private final String roiCoordinateType;
    private final BigDecimal roiX;
    private final BigDecimal roiY;
    private final BigDecimal roiWidth;
    private final BigDecimal roiHeight;
    private final Boolean qualityGateEnabled;
    private final LocalDateTime createdAt;
}
