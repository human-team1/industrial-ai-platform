package com.example.factoryguard.application.dto.inspection;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiInspectionCommand {

    private final Long inspectionId;
    private final String fileKey;
    private final Long targetId;
    private final Model model;
    private final Roi roi;
    private final boolean qualityGateEnabled;
    private final Threshold threshold;

    @Getter
    @Builder
    public static class Model {
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
    public static class Roi {
        private final String roiMode;
        private final String roiCoordinateType;
        private final Double roiX;
        private final Double roiY;
        private final Double roiWidth;
        private final Double roiHeight;
    }

    @Getter
    @Builder
    public static class Threshold {
        private final double anomalyThreshold;
        private final double lowConfidenceThreshold;
    }
}
