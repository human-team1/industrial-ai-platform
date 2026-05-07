package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.AiInspectionResult;
import com.example.factoryguard.application.dto.inspection.AiRealtimeInspectionCommand;
import com.example.factoryguard.application.dto.inspection.ResolvedInspectionModelArtifacts;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.dto.inspection.SubmitRealtimeInspectionCommand;
import com.example.factoryguard.application.dto.notification.CreateNotificationCommand;
import com.example.factoryguard.application.port.in.inspection.SubmitRealtimeInspectionUseCase;
import com.example.factoryguard.application.port.in.notification.CreateNotificationUseCase;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.inspection.LoadCameraSourcePort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionResultPort;
import com.example.factoryguard.application.port.out.review.SaveReviewQueuePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.model.AnalysisTarget;
import com.example.factoryguard.domain.inspection.model.CameraSource;
import com.example.factoryguard.domain.inspection.model.DecisionCalculator;
import com.example.factoryguard.domain.inspection.model.DecisionCode;
import com.example.factoryguard.domain.inspection.model.InputSourceType;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.model.InspectionResult;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.model.RunType;
import com.example.factoryguard.domain.notification.vo.NotificationSeverity;
import com.example.factoryguard.domain.notification.vo.NotificationType;
import com.example.factoryguard.domain.review.model.ReviewQueue;
import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.application.exception.ai.AiInvalidRequestException;
import com.example.factoryguard.application.exception.ai.AiServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubmitRealtimeInspectionService implements SubmitRealtimeInspectionUseCase {

    private static final String RESULT_STATUS_SUCCESS = "SUCCESS";
    private static final String RESULT_STATUS_REVIEW_REQUIRED = "REVIEW_REQUIRED";
    private static final String RESULT_STATUS_FAILED = "FAILED";

    private final FindUserByIdPort findUserByIdPort;
    private final LoadAnalysisTargetPort loadAnalysisTargetPort;
    private final LoadCameraSourcePort loadCameraSourcePort;
    private final SaveInspectionResultPort saveInspectionResultPort;
    private final SaveReviewQueuePort saveReviewQueuePort;
    private final CallAiInspectionPort callAiInspectionPort;
    private final ResolveInspectionThresholdService resolveInspectionThresholdService;
    private final InspectionRunRecorder runRecorder;
    private final InspectionInputRecorder inputRecorder;
    private final InspectionEventLogger eventLogger;
    private final DecisionProperties decisionProperties;
    private final CreateNotificationUseCase createNotificationUseCase;
    private final SessionValidationService sessionValidationService;
    private final InferenceModelArtifactResolver inferenceModelArtifactResolver;

    @Override
    public SubmitInspectionResult execute(SubmitRealtimeInspectionCommand command) {
        sessionValidationService.validate(command.getUserId(), command.getSessionId());
        User user = validateUserStatus(command.getUserId());
        if (command.getCameraId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (command.getDeploymentId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "deploymentId는 필수입니다.");
        }
        ResolvedThreshold resolved = resolveInspectionThresholdService.resolve(
                command.getUserId(), command.getThresholdId());
        if (command.getTargetId() != null) {
            AnalysisTarget target = loadAnalysisTargetPort.findById(command.getTargetId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND));
            if (!target.getOrganizationId().equals(user.getOrganizationId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }
        CameraSource camera = loadCameraSourcePort.findById(command.getCameraId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMERA_NOT_FOUND));
        if (!camera.getOrganizationId().equals(user.getOrganizationId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        ResolvedInspectionModelArtifacts resolvedModel = inferenceModelArtifactResolver.resolve(
                user.getOrganizationId(),
                command.getTargetId(),
                command.getDeploymentId()
        );

        InspectionRun run = runRecorder.create(InspectionRun.builder()
                .organizationId(user.getOrganizationId())
                .userId(command.getUserId())
                .targetId(command.getTargetId())
                .runType(RunType.REALTIME)
                .inputType("CAMERA")
                .sourceType("CAMERA")
                .sourceId(InspectionRunSourceMetadata.forRealtime(camera.getCameraId(), resolvedModel.getDeployment().getDeploymentId()))
                .runStatus(RunStatus.PROCESSING)
                .appliedThreshold(BigDecimal.valueOf(resolved.getAnomalyThreshold()))
                .idempotencyKey(UUID.randomUUID().toString())
                .startedAt(LocalDateTime.now())
                .build());
        Long runId = run.getInspectionId();

        try {
            inputRecorder.record(InspectionInput.builder()
                    .inspectionId(runId)
                    .sourceType(InputSourceType.CAMERA)
                    .cameraId(camera.getCameraId())
                    .streamUrl(camera.getStreamUrl())
                    .sourceName(camera.getCameraName())
                    .build());

            eventLogger.log(runId, InspectionEventType.REALTIME_STARTED, "실시간 탐지가 시작되었습니다.");
            eventLogger.log(runId, InspectionEventType.INPUT_SAVED, "camera input persisted");

            return SubmitInspectionResult.accepted(run, false);

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Unexpected error during realtime inspection submission, runId={}", runId, e);
            saveFailedResult(runId, resolved, "UNEXPECTED_ERROR: " + safeMessage(e));
            runRecorder.markFailed(runId, "UNEXPECTED_ERROR");
            eventLogger.logFailure(runId, InspectionEventType.FAILED, "unexpected: " + safeMessage(e));
            notifySystemError(command.getUserId(), runId);
            throw new BusinessException(ErrorCode.INSPECTION_FAILED);
        }
    }

    private void saveFailedResult(Long runId, ResolvedThreshold resolved, String failureReason) {
        try {
            saveInspectionResultPort.save(InspectionResult.builder()
                    .inspectionId(runId)
                    .score(null)
                    .confidence(null)
                    .decisionCode(null)
                    .finalDecisionCode(null)
                    .resultStatus(RESULT_STATUS_FAILED)
                    .thresholdSource(resolved.getSource().name())
                    .thresholdId(resolved.getThresholdId())
                    .thresholdVersion(resolved.getThresholdVersion())
                    .modelVersionId(null)
                    .failureReason(failureReason)
                    .build());
        } catch (Exception persistEx) {
            log.error("Failed to persist FAILED InspectionResult, runId={}", runId, persistEx);
        }
    }

    private void notifySystemError(Long userId, Long inspectionId) {
        if (userId == null || inspectionId == null) return;
        try {
            createNotificationUseCase.execute(CreateNotificationCommand.builder()
                    .userId(userId)
                    .notificationType(NotificationType.SYSTEM_ERROR)
                    .severity(NotificationSeverity.WARNING)
                    .title("검사 처리 중 오류가 발생했습니다.")
                    .message("검사 처리 중 오류가 발생했습니다. 잠시 후 다시 시도하거나 관리자에게 문의해 주세요.")
                    .relatedType("INSPECTION")
                    .relatedId(inspectionId)
                    .targetUrl("/inspections/" + inspectionId)
                    .dedupKey("inspection:" + inspectionId + ":system-error")
                    .build());
        } catch (Exception notifyEx) {
            log.warn("Failed to create SYSTEM_ERROR notification, userId={}, inspectionId={}",
                    userId, inspectionId, notifyEx);
        }
    }

    private String safeMessage(Throwable t) {
        String msg = t.getMessage();
        if (msg == null) return t.getClass().getSimpleName();
        return msg.length() > 1000 ? msg.substring(0, 1000) : msg;
    }
    private User validateUserStatus(Long userId) {
        User user = findUserByIdPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        switch (user.getStatus()) {
            case ACTIVE -> { return user; }
            case PENDING -> throw new BusinessException(ErrorCode.PENDING_APPROVAL);
            case REJECTED -> throw new BusinessException(ErrorCode.ACCOUNT_REJECTED);
            default -> throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
