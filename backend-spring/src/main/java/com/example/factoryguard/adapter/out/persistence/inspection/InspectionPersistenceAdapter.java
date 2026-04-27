package com.example.factoryguard.adapter.out.persistence.inspection;

import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionResultPort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionRunPort;
import com.example.factoryguard.domain.inspection.model.AnalysisTarget;
import com.example.factoryguard.domain.inspection.model.InspectionResult;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InspectionPersistenceAdapter implements
        LoadAnalysisTargetPort,
        SaveInspectionRunPort,
        SaveInspectionResultPort {

    private final AnalysisTargetJpaRepository analysisTargetJpaRepository;
    private final InspectionRunJpaRepository inspectionRunJpaRepository;
    private final InspectionResultJpaRepository inspectionResultJpaRepository;

    @Override
    public Optional<AnalysisTarget> findById(Long targetId) {
        return analysisTargetJpaRepository.findById(targetId)
                .map(e -> AnalysisTarget.builder()
                        .targetId(e.getTargetId())
                        .organizationId(e.getOrganizationId())
                        .targetName(e.getTargetName())
                        .equipmentName(e.getEquipmentName())
                        .productName(e.getProductName())
                        .build());
    }

    @Override
    public InspectionRun save(InspectionRun run) {
        InspectionRunJpaEntity saved;
        if (run.getInspectionId() != null) {
            InspectionRunJpaEntity existing = inspectionRunJpaRepository.findById(run.getInspectionId())
                    .orElseThrow();
            existing.updateStatus(run.getRunStatus());
            saved = existing;
        } else {
            saved = inspectionRunJpaRepository.save(
                    InspectionRunJpaEntity.builder()
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
                            .startedAt(run.getStartedAt())
                            .build()
            );
        }
        return InspectionRun.builder()
                .inspectionId(saved.getInspectionId())
                .organizationId(saved.getOrganizationId())
                .userId(saved.getUserId())
                .targetId(saved.getTargetId())
                .runType(saved.getRunType())
                .inputType(saved.getInputType())
                .sourceType(saved.getSourceType())
                .sourceId(saved.getSourceId())
                .runStatus(saved.getRunStatus())
                .appliedThreshold(saved.getAppliedThreshold())
                .idempotencyKey(saved.getIdempotencyKey())
                .startedAt(saved.getStartedAt())
                .build();
    }

    @Override
    public InspectionResult save(InspectionResult result) {
        InspectionResultJpaEntity saved = inspectionResultJpaRepository.save(
                InspectionResultJpaEntity.builder()
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
                        .build()
        );
        return InspectionResult.builder()
                .resultId(saved.getResultId())
                .inspectionId(saved.getInspectionId())
                .score(saved.getScore())
                .confidence(saved.getConfidence())
                .decisionCode(saved.getDecisionCode())
                .finalDecisionCode(saved.getFinalDecisionCode())
                .resultStatus(saved.getResultStatus())
                .thresholdSource(saved.getThresholdSource())
                .thresholdId(saved.getThresholdId())
                .thresholdVersion(saved.getThresholdVersion())
                .modelVersionId(saved.getModelVersionId())
                .failureReason(saved.getFailureReason())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}