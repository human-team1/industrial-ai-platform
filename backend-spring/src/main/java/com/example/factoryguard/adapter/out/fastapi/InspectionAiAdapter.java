package com.example.factoryguard.adapter.out.fastapi;

import com.example.factoryguard.adapter.out.fastapi.client.FastApiClient;
import com.example.factoryguard.adapter.out.fastapi.request.AiInspectionRequest;
import com.example.factoryguard.adapter.out.fastapi.response.AiInspectionResponse;
import com.example.factoryguard.application.dto.inspection.AiInspectionCommand;
import com.example.factoryguard.application.dto.inspection.AiInspectionResult;
import com.example.factoryguard.application.dto.inspection.AiRealtimeInspectionCommand;
import com.example.factoryguard.application.exception.ai.AiServerException;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class InspectionAiAdapter implements CallAiInspectionPort {

    private final FastApiClient fastApiClient;
    private final RestTemplate restTemplate;

    @Override
    public AiInspectionResult call(AiInspectionCommand command) throws TimeoutException {
        String url = fastApiClient.baseUrl() + "/ai/v1/internal/vision/infer-image";
        AiInspectionRequest request = toRequest(command);
        try {
            AiInspectionResponse response = restTemplate.postForObject(url, request, AiInspectionResponse.class);
            return toResult(response);
        } catch (ResourceAccessException e) {
            if (hasCause(e, SocketTimeoutException.class)) {
                throw (TimeoutException) new TimeoutException(e.getMessage()).initCause(e);
            }
            throw new AiServerException("AI server connection failed: " + e.getMessage(), e);
        }
    }

    @Override
    public AiInspectionResult callRealtime(AiRealtimeInspectionCommand command) {
        throw new UnsupportedOperationException("realtime inspection not implemented yet");
    }

    private AiInspectionRequest toRequest(AiInspectionCommand command) {
        return AiInspectionRequest.builder()
                .inspectionId(command.getInspectionId())
                .fileKey(command.getFileKey())
                .targetId(command.getTargetId())
                .model(AiInspectionRequest.ModelRequest.builder()
                        .modelVersionId(command.getModel().getModelVersionId())
                        .modelCategory(command.getModel().getModelCategory())
                        .modelProfile(command.getModel().getModelProfile())
                        .framework(command.getModel().getFramework())
                        .inputSize(command.getModel().getInputSize())
                        .ckptFileKey(command.getModel().getCkptFileKey())
                        .configFileKey(command.getModel().getConfigFileKey())
                        .memoryBankFileKey(command.getModel().getMemoryBankFileKey())
                        .labelsFileKey(command.getModel().getLabelsFileKey())
                        .build())
                .roi(AiInspectionRequest.RoiRequest.builder()
                        .roiMode(command.getRoi().getRoiMode())
                        .roiCoordinateType(command.getRoi().getRoiCoordinateType())
                        .roiX(command.getRoi().getRoiX())
                        .roiY(command.getRoi().getRoiY())
                        .roiWidth(command.getRoi().getRoiWidth())
                        .roiHeight(command.getRoi().getRoiHeight())
                        .build())
                .qualityGateEnabled(command.isQualityGateEnabled())
                .threshold(AiInspectionRequest.ThresholdRequest.builder()
                        .anomalyThreshold(command.getThreshold().getAnomalyThreshold())
                        .lowConfidenceThreshold(command.getThreshold().getLowConfidenceThreshold())
                        .build())
                .build();
    }

    private AiInspectionResult toResult(AiInspectionResponse response) {
        if (response == null || response.getData() == null) {
            return null;
        }
        return AiInspectionResult.builder()
                .score(response.getData().getScore())
                .confidence(response.getData().getConfidence())
                .modelVersionId(response.getData().getModelVersionId())
                .decisionCode(response.getData().getDecisionCode())
                .quality(response.getData().getQuality() == null ? null : AiInspectionResult.Quality.builder()
                        .status(response.getData().getQuality().getStatus())
                        .reason(response.getData().getQuality().getReason())
                        .brightness(response.getData().getQuality().getBrightness())
                        .contrast(response.getData().getQuality().getContrast())
                        .blurScore(response.getData().getQuality().getBlurScore())
                        .saturation(response.getData().getQuality().getSaturation())
                        .build())
                .artifacts(response.getData().getArtifacts() == null ? List.of() : response.getData().getArtifacts().stream()
                        .map(artifact -> AiInspectionResult.Artifact.builder()
                                .artifactType(artifact.getArtifactType())
                                .fileKey(artifact.getFileKey())
                                .build())
                        .toList())
                .processedAt(response.getData().getProcessedAt())
                .build();
    }

    private static boolean hasCause(Throwable throwable, Class<?> targetType) {
        Throwable current = throwable;
        while (current != null) {
            if (targetType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
