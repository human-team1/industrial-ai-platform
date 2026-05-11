package com.example.factoryguard.application.dto.inspection;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AiInspectionResult {

    private final Double score;
    private final double confidence;
    private final Long modelVersionId;
    private final String imagePath;
    private final String categoryType;
    private final String category;
    private final String modelProfile;
    private final String modelName;
    private final Double imageThreshold;
    private final String predictedLabel;
    private final String heatmapPath;
    private final Double pixelThreshold;
    private final Long inferenceTime;
    private final String decisionCode;
    private final Quality quality;
    private final List<Artifact> artifacts;
    private final LocalDateTime processedAt;

    @Getter
    @Builder
    public static class Quality {
        private final String status;
        private final String reason;
        private final Double brightness;
        private final Double contrast;
        private final Double blurScore;
        private final Double saturation;
    }

    @Getter
    @Builder
    public static class Artifact {
        private final String artifactType;
        private final String fileKey;
    }
}
