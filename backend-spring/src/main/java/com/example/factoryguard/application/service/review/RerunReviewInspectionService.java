package com.example.factoryguard.application.service.review;

import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.dto.review.RerunReviewInspectionCommand;
import com.example.factoryguard.application.port.in.review.RerunReviewInspectionUseCase;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionInputPort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.application.port.out.result.ResultQueryPort;
import com.example.factoryguard.application.port.out.review.LoadReviewQueuePort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.application.service.inspection.InferenceModelArtifactResolver;
import com.example.factoryguard.application.service.inspection.InspectionRunSourceMetadata;
import com.example.factoryguard.application.service.inspection.InspectionUploadTransactionService;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.model.RunType;
import com.example.factoryguard.domain.review.model.ReviewQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RerunReviewInspectionService implements RerunReviewInspectionUseCase {

    private final SessionValidationService sessionValidationService;
    private final LoadReviewQueuePort loadReviewQueuePort;
    private final ResultQueryPort resultQueryPort;
    private final LoadInspectionRunPort loadInspectionRunPort;
    private final LoadInspectionInputPort loadInspectionInputPort;
    private final InferenceModelArtifactResolver inferenceModelArtifactResolver;
    private final InspectionUploadTransactionService inspectionUploadTransactionService;

    @Override
    @Transactional
    public SubmitInspectionResult execute(RerunReviewInspectionCommand command) {
        sessionValidationService.validate(command.getUserId(), command.getSessionId());
        if (command.getDeploymentId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "deploymentId는 필수입니다.");
        }

        ReviewQueue queue = loadReviewQueuePort.findById(command.getReviewQueueId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "재검토 대상을 찾을 수 없습니다."));
        ResultDetailResponse detail = resultQueryPort.findDetail(queue.getResultId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "검사 결과를 찾을 수 없습니다."));
        InspectionRun originalRun = loadInspectionRunPort.findRunById(detail.getInspectionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_NOT_FOUND));
        InspectionInput originalInput = loadInspectionInputPort.findByInspectionId(originalRun.getInspectionId()).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INSPECTION_NOT_FOUND));

        inferenceModelArtifactResolver.resolve(originalRun.getOrganizationId(), originalRun.getTargetId(), command.getDeploymentId());

        InspectionRun rerun = inspectionUploadTransactionService.createPendingRun(InspectionRun.builder()
                .organizationId(originalRun.getOrganizationId())
                .userId(command.getUserId())
                .targetId(originalRun.getTargetId())
                .runType(RunType.UPLOAD)
                .inputType(originalRun.getInputType())
                .sourceType(originalRun.getSourceType())
                .sourceId(InspectionRunSourceMetadata.forUpload(
                        originalInput.getSourceName() == null ? "review-rerun" : originalInput.getSourceName(),
                        command.getDeploymentId()))
                .runStatus(RunStatus.PENDING)
                .appliedThreshold(originalRun.getAppliedThreshold())
                .idempotencyKey(UUID.randomUUID().toString())
                .startedAt(LocalDateTime.now())
                .build());

        inspectionUploadTransactionService.persistExistingInputAndMarkProcessing(
                rerun.getInspectionId(),
                originalInput.toBuilder().inspectionId(rerun.getInspectionId()).build(),
                originalInput.getSourceName() == null ? "review-rerun" : originalInput.getSourceName()
        );

        return SubmitInspectionResult.accepted(rerun.toBuilder().runStatus(RunStatus.PROCESSING).build(), false);
    }
}
