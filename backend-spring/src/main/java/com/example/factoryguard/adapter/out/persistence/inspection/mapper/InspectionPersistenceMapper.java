package com.example.factoryguard.adapter.out.persistence.inspection.mapper;

import com.example.factoryguard.adapter.out.persistence.inspection.AnalysisTargetJpaEntity;
import com.example.factoryguard.adapter.out.persistence.inspection.CameraSourceJpaEntity;
import com.example.factoryguard.adapter.out.persistence.inspection.InspectionEventLogJpaEntity;
import com.example.factoryguard.adapter.out.persistence.inspection.InspectionInputJpaEntity;
import com.example.factoryguard.adapter.out.persistence.inspection.InspectionResultJpaEntity;
import com.example.factoryguard.adapter.out.persistence.inspection.InspectionRunJpaEntity;
import com.example.factoryguard.domain.inspection.model.AnalysisTarget;
import com.example.factoryguard.domain.inspection.model.CameraSource;
import com.example.factoryguard.domain.inspection.model.InspectionEventLog;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.model.InspectionResult;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import org.springframework.stereotype.Component;

@Component
public class InspectionPersistenceMapper {

    public AnalysisTarget toDomain(AnalysisTargetJpaEntity e) {
        return AnalysisTarget.builder()
                .targetId(e.getTargetId())
                .organizationId(e.getOrganizationId())
                .targetName(e.getTargetName())
                .equipmentName(e.getEquipmentName())
                .productName(e.getProductName())
                .build();
    }

    public InspectionRunJpaEntity toEntity(InspectionRun run) {
        return InspectionRunJpaEntity.builder()
                .organizationId(run.getOrganizationId())
                .userId(run.getUserId())
                .targetId(run.getTargetId())
                .runType(run.getRunType())
                .inputType(run.getInputType())
                .sourceType(run.getSourceType())
                .sourceId(run.getSourceId())
                .runStatus(run.getRunStatus())
                .appliedThreshold(run.getAppliedThreshold())
                .idempotencyKey(run.getIdempotencyKey())
                .payloadFingerprint(run.getPayloadFingerprint())
                .startedAt(run.getStartedAt())
                .build();
    }

    public InspectionRun toDomain(InspectionRunJpaEntity e) {
        return InspectionRun.builder()
                .inspectionId(e.getInspectionId())
                .organizationId(e.getOrganizationId())
                .userId(e.getUserId())
                .targetId(e.getTargetId())
                .runType(e.getRunType())
                .inputType(e.getInputType())
                .sourceType(e.getSourceType())
                .sourceId(e.getSourceId())
                .runStatus(e.getRunStatus())
                .appliedThreshold(e.getAppliedThreshold())
                .idempotencyKey(e.getIdempotencyKey())
                .payloadFingerprint(e.getPayloadFingerprint())
                .errorCode(e.getErrorCode())
                .startedAt(e.getStartedAt())
                .completedAt(e.getCompletedAt())
                .build();
    }

    public InspectionResultJpaEntity toEntity(InspectionResult result) {
        return InspectionResultJpaEntity.builder()
                .inspectionId(result.getInspectionId())
                .score(result.getScore())
                .confidence(result.getConfidence())
                .decisionCode(result.getDecisionCode())
                .finalDecisionCode(result.getFinalDecisionCode())
                .resultStatus(result.getResultStatus())
                .thresholdSource(result.getThresholdSource())
                .thresholdId(result.getThresholdId())
                .thresholdVersion(result.getThresholdVersion())
                .modelVersionId(result.getModelVersionId())
                .failureReason(result.getFailureReason())
                .build();
    }

    public InspectionResult toDomain(InspectionResultJpaEntity e) {
        return InspectionResult.builder()
                .resultId(e.getResultId())
                .inspectionId(e.getInspectionId())
                .score(e.getScore())
                .confidence(e.getConfidence())
                .decisionCode(e.getDecisionCode())
                .finalDecisionCode(e.getFinalDecisionCode())
                .resultStatus(e.getResultStatus())
                .thresholdSource(e.getThresholdSource())
                .thresholdId(e.getThresholdId())
                .thresholdVersion(e.getThresholdVersion())
                .modelVersionId(e.getModelVersionId())
                .failureReason(e.getFailureReason())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public InspectionInputJpaEntity toEntity(InspectionInput input) {
        return InspectionInputJpaEntity.builder()
                .inspectionId(input.getInspectionId())
                .fileId(input.getFileId())
                .cameraId(input.getCameraId())
                .streamUrl(input.getStreamUrl())
                .sourceType(input.getSourceType())
                .sourceName(input.getSourceName())
                .mimeType(input.getMimeType())
                .durationSec(input.getDurationSec())
                .frameCount(input.getFrameCount())
                .build();
    }

    public InspectionInput toDomain(InspectionInputJpaEntity e) {
        return InspectionInput.builder()
                .inspectionInputId(e.getInspectionInputId())
                .inspectionId(e.getInspectionId())
                .fileId(e.getFileId())
                .cameraId(e.getCameraId())
                .streamUrl(e.getStreamUrl())
                .sourceType(e.getSourceType())
                .sourceName(e.getSourceName())
                .mimeType(e.getMimeType())
                .durationSec(e.getDurationSec())
                .frameCount(e.getFrameCount())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public InspectionEventLogJpaEntity toEntity(InspectionEventLog log) {
        return InspectionEventLogJpaEntity.builder()
                .inspectionId(log.getInspectionId())
                .eventType(log.getEventType())
                .message(log.getMessage())
                .build();
    }

    public InspectionEventLog toDomain(InspectionEventLogJpaEntity e) {
        return InspectionEventLog.builder()
                .eventId(e.getEventId())
                .inspectionId(e.getInspectionId())
                .eventType(e.getEventType())
                .message(e.getMessage())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public CameraSourceJpaEntity toEntity(CameraSource camera) {
        return CameraSourceJpaEntity.builder()
                .organizationId(camera.getOrganizationId())
                .userId(camera.getUserId())
                .cameraName(camera.getCameraName())
                .streamUrl(camera.getStreamUrl())
                .status(camera.getStatus())
                .build();
    }

    public CameraSource toDomain(CameraSourceJpaEntity e) {
        return CameraSource.builder()
                .cameraId(e.getCameraId())
                .organizationId(e.getOrganizationId())
                .userId(e.getUserId())
                .cameraName(e.getCameraName())
                .streamUrl(e.getStreamUrl())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
