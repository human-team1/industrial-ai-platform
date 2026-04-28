package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.AiInspectionResponse;
import com.example.factoryguard.application.dto.inspection.AiRealtimeInspectionRequest;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.dto.inspection.SubmitRealtimeInspectionCommand;
import com.example.factoryguard.application.port.in.inspection.SubmitRealtimeInspectionUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.inspection.LoadCameraSourcePort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionResultPort;
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
import com.example.factoryguard.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubmitRealtimeInspectionService implements SubmitRealtimeInspectionUseCase {

    private final TokenStorePort tokenStorePort;
    private final FindUserByIdPort findUserByIdPort;
    private final LoadAnalysisTargetPort loadAnalysisTargetPort;
    private final LoadCameraSourcePort loadCameraSourcePort;
    private final SaveInspectionResultPort saveInspectionResultPort;
    private final CallAiInspectionPort callAiInspectionPort;
    private final ResolveInspectionThresholdService resolveInspectionThresholdService;
    private final InspectionRunRecorder runRecorder;
    private final InspectionInputRecorder inputRecorder;
    private final InspectionEventLogger eventLogger;

    @Override
    public SubmitInspectionResult execute(SubmitRealtimeInspectionCommand command) {
        // ① 사전 검증
        validateSession(command.getUserId(), command.getSessionId());
        User user = validateUserStatus(command.getUserId());
        ResolvedThreshold resolved = resolveInspectionThresholdService.resolve(
                command.getUserId(), command.getThresholdId());
        AnalysisTarget target = loadAnalysisTargetPort.findById(command.getTargetId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND));
        if (!target.getOrganizationId().equals(user.getOrganizationId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        CameraSource camera = loadCameraSourcePort.findById(command.getCameraId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CAMERA_NOT_FOUND));
        if (!camera.getOrganizationId().equals(user.getOrganizationId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // ② RUN 생성 (REQUIRES_NEW)
        InspectionRun run = runRecorder.create(InspectionRun.builder()
                .organizationId(user.getOrganizationId())
                .userId(command.getUserId())
                .targetId(command.getTargetId())
                .runType(RunType.REALTIME)
                .inputType("CAMERA")
                .sourceType("CAMERA")
                .sourceId(String.valueOf(camera.getCameraId()))
                .runStatus(RunStatus.PENDING)
                .appliedThreshold(resolved.getAnomalyThreshold())
                .idempotencyKey(UUID.randomUUID().toString())
                .startedAt(LocalDateTime.now())
                .build());
        Long runId = run.getInspectionId();

        try {
            // ③ INPUT 저장 (REQUIRES_NEW) — sourceType=CAMERA
            inputRecorder.record(InspectionInput.builder()
                    .inspectionId(runId)
                    .sourceType(InputSourceType.CAMERA)
                    .cameraId(camera.getCameraId())
                    .streamUrl(camera.getStreamUrl())
                    .sourceName(camera.getCameraName())
                    .build());

            eventLogger.log(runId, InspectionEventType.UPLOAD_RECEIVED, "camera=" + camera.getCameraId());
            eventLogger.log(runId, InspectionEventType.INPUT_SAVED, "camera input persisted");

            runRecorder.transitTo(runId, RunStatus.PROCESSING);
            eventLogger.log(runId, InspectionEventType.PROCESS_STARTED, "processing started");

            eventLogger.log(runId, InspectionEventType.AI_CALLED, "realtime ai called");
            AiInspectionResponse aiResponse;
            try {
                aiResponse = callAiInspectionPort.callRealtime(new AiRealtimeInspectionRequest(
                        camera.getCameraId(),
                        camera.getStreamUrl(),
                        resolved.getAnomalyThreshold(),
                        resolved.getLowConfidenceThreshold()
                ));
            } catch (Exception aiEx) {
                boolean timeout = aiEx instanceof TimeoutException
                        || (aiEx.getCause() != null && aiEx.getCause() instanceof TimeoutException);
                if (timeout) {
                    eventLogger.logFailure(runId, InspectionEventType.AI_TIMEOUT, safeMessage(aiEx));
                    runRecorder.markFailed(runId, "AI_TIMEOUT");
                } else {
                    eventLogger.logFailure(runId, InspectionEventType.AI_FAILED, safeMessage(aiEx));
                    runRecorder.markFailed(runId, "AI_FAILED");
                }
                eventLogger.logFailure(runId, InspectionEventType.FAILED, timeout ? "ai timeout" : "ai failed");
                throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
            }

            DecisionCode decision = DecisionCalculator.decide(
                    aiResponse.getScore(),
                    aiResponse.getConfidence(),
                    resolved.getAnomalyThreshold(),
                    resolved.getLowConfidenceThreshold()
            );
            InspectionResult result = saveInspectionResultPort.save(InspectionResult.builder()
                    .inspectionId(runId)
                    .score(aiResponse.getScore())
                    .confidence(aiResponse.getConfidence())
                    .decisionCode(decision)
                    .finalDecisionCode(decision)
                    .resultStatus("COMPLETED")
                    .thresholdSource(resolved.getSource().name())
                    .thresholdId(resolved.getThresholdId())
                    .build());
            eventLogger.log(runId, InspectionEventType.RESULT_SAVED, "result saved");

            runRecorder.transitTo(runId, RunStatus.COMPLETED);
            eventLogger.log(runId, InspectionEventType.COMPLETED, "completed");

            return SubmitInspectionResult.of(run.toBuilder().runStatus(RunStatus.COMPLETED).build(), result);

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Unexpected error during realtime inspection submission, runId={}", runId, e);
            runRecorder.markFailed(runId, "UNEXPECTED_ERROR");
            eventLogger.logFailure(runId, InspectionEventType.FAILED, "unexpected: " + safeMessage(e));
            throw new BusinessException(ErrorCode.INSPECTION_FAILED);
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
