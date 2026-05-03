package com.example.factoryguard.adapter.out.fastapi.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AiInspectionResponse {

    private boolean success;
    private ResponseData data;
    private String message;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ResponseData {
        private Long inspectionId;
        private Long modelVersionId;
        private Double score;
        private double confidence;
        private String decisionCode;
        private Quality quality;
        private List<Artifact> artifacts;
        private LocalDateTime processedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Quality {
        private String status;
        private String reason;
        private Double brightness;
        private Double contrast;
        private Double blurScore;
        private Double saturation;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Artifact {
        private String artifactType;
        private String fileKey;
    }
}
