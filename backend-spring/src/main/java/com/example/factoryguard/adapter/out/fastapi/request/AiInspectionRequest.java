package com.example.factoryguard.adapter.out.fastapi.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiInspectionRequest {

    private final Long inspectionId;
    private final String fileKey;
    private final Long targetId;
    private final ModelRequest model;
    private final RoiRequest roi;
    private final boolean qualityGateEnabled;
    private final ThresholdRequest threshold;

    @Getter
    @Builder
    public static class ModelRequest {
        private final Long modelVersionId;
        private final String modelCategory;
        private final String modelProfile;
        private final String framework;
        private final String inputSize;
        private final String ckptFileKey;
        private final String configFileKey;
        private final String memoryBankFileKey;
        private final String labelsFileKey;
    }

    @Getter
    @Builder
    public static class RoiRequest {
        private final String roiMode;
        private final String roiCoordinateType;
        private final Double roiX;
        private final Double roiY;
        private final Double roiWidth;
        private final Double roiHeight;
    }

    @Getter
    @Builder
    public static class ThresholdRequest {
        private final double anomalyThreshold;
        private final double lowConfidenceThreshold;
    }
}
