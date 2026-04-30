package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.AiInspectionResult;
import com.example.factoryguard.application.dto.inspection.AiRealtimeInspectionCommand;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.dto.inspection.SubmitRealtimeInspectionCommand;
import com.example.factoryguard.application.port.in.inspection.SubmitRealtimeInspectionUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.inspection.LoadCameraSourcePort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionResultPort;
import com.example.factoryguard.application.port.out.review.SaveReviewQueuePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
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

    private final TokenStorePort tokenStorePort;
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

    @Override
    public SubmitInspectionResult execute(SubmitRealtimeInspectionCommand command) {
        validateSession(command.getUserId(), command.getSessionId());
        User user = validateUserStatus(command.getUserId());
        if (command.getCameraId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
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

        InspectionRun run = runRecorder.create(InspectionRun.builder()
                .organizationId(user.getOrganizationId())
                .userId(command.getUserId())
                .targetId(command.getTargetId())
                .runType(RunType.REALTIME)
                .inputType("CAMERA")
                .sourceType("CAMERA")
                .sourceId(String.valueOf(camera.getCameraId()))
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

    private String safeMessage(Throwable t) {
        String msg = t.getMessage();
        if (msg == null) return t.getClass().getSimpleName();
        return msg.length() > 1000 ? msg.substring(0, 1000) : msg;
    }

    private void validateSession(Long userId, String sessionId) {
        if (sessionId == null) {
            throw new BusinessException(ErrorCode.SESSION_INVALID);
        }
        String current = tokenStorePort.getSessionId(userId).orElse(null);
        if (current == null || current.isBlank() || !current.equals(sessionId)) {
            throw new BusinessException(ErrorCode.SESSION_INVALID);
        }
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
