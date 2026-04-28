package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.*;
import com.example.factoryguard.application.port.in.inspection.SubmitInspectionUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.inspection.*;
import com.example.factoryguard.application.port.out.review.SaveReviewQueuePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.model.*;
import com.example.factoryguard.domain.review.model.ReviewQueue;
import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import com.example.factoryguard.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubmitInspectionService implements SubmitInspectionUseCase {

    private static final String RESULT_STATUS_SUCCESS = "SUCCESS";
    private static final String RESULT_STATUS_REVIEW_REQUIRED = "REVIEW_REQUIRED";
    private static final String RESULT_STATUS_FAILED = "FAILED";

    private final TokenStorePort tokenStorePort;
    private final FindUserByIdPort findUserByIdPort;
    private final LoadAnalysisTargetPort loadAnalysisTargetPort;
    private final SaveInspectionResultPort saveInspectionResultPort;
    private final SaveReviewQueuePort saveReviewQueuePort;
    private final CallAiInspectionPort callAiInspectionPort;
    private final ResolveInspectionThresholdService resolveInspectionThresholdService;
    private final InspectionRunRecorder runRecorder;
    private final InspectionInputRecorder inputRecorder;
    private final InspectionEventLogger eventLogger;
    private final DecisionProperties decisionProperties;

    @Override
    public SubmitInspectionResult execute(SubmitInspectionCommand command) {
        validateSession(command.getUserId(), command.getSessionId());
        User user = validateUserStatus(command.getUserId());
        ResolvedThreshold resolved = resolveInspectionThresholdService.resolve(
                command.getUserId(), command.getThresholdId());
        AnalysisTarget target = loadAnalysisTargetPort.findById(command.getTargetId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND));
        if (!target.getOrganizationId().equals(user.getOrganizationId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        InspectionRun run = runRecorder.create(InspectionRun.builder()
                .organizationId(user.getOrganizationId())
                .userId(command.getUserId())
                .targetId(command.getTargetId())
                .runType(RunType.UPLOAD)
                .inputType("FILE")
                .sourceType("UPLOAD")
                .sourceId(command.getOriginalFileName())
                .runStatus(RunStatus.PENDING)
                .appliedThreshold(resolved.getAnomalyThreshold())
                .idempotencyKey(UUID.randomUUID().toString())
                .startedAt(LocalDateTime.now())
                .build());
        Long runId = run.getInspectionId();

        try {
            inputRecorder.record(InspectionInput.builder()
                    .inspectionId(runId)
                    .sourceType(InputSourceType.FILE)
                    .sourceName(command.getOriginalFileName())
                    .mimeType(command.getMimeType())
                    .build());

            eventLogger.log(runId, InspectionEventType.UPLOAD_RECEIVED, command.getOriginalFileName());
            eventLogger.log(runId, InspectionEventType.INPUT_SAVED, "input persisted");

            runRecorder.transitTo(runId, RunStatus.PROCESSING);
            eventLogger.log(runId, InspectionEventType.PROCESS_STARTED, "processing started");

            eventLogger.log(runId, InspectionEventType.AI_CALLED, "ai called");
            AiInspectionResponse aiResponse;
            try {
                aiResponse = callAiInspectionPort.call(new AiInspectionRequest(
                        command.getFileUrl(),
                        resolved.getAnomalyThreshold(),
                        resolved.getLowConfidenceThreshold()
                ));
            } catch (TimeoutException aiEx) {
                String detail = "AI_TIMEOUT: " + safeMessage(aiEx);
                saveFailedResult(runId, resolved, detail);
                eventLogger.logFailure(runId, InspectionEventType.AI_TIMEOUT, detail);
                runRecorder.markFailed(runId, ErrorCode.AI_TIMEOUT.name());
                eventLogger.logFailure(runId, InspectionEventType.FAILED, "ai timeout");
                throw new BusinessException(ErrorCode.AI_TIMEOUT);
            } catch (RestClientException aiEx) {
                String detail = "AI_SERVER_ERROR: " + safeMessage(aiEx);
                saveFailedResult(runId, resolved, detail);
                eventLogger.logFailure(runId, InspectionEventType.AI_FAILED, detail);
                runRecorder.markFailed(runId, ErrorCode.AI_SERVER_ERROR.name());
                eventLogger.logFailure(runId, InspectionEventType.FAILED, "ai failed");
                throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
            }

            DecisionCalculator.DecisionResult decision = DecisionCalculator.decide(
                    aiResponse.getScore(),
                    aiResponse.getConfidence(),
                    resolved.getAnomalyThreshold(),
                    resolved.getLowConfidenceThreshold(),
                    decisionProperties.getBoundaryMargin()
            );

            String resultStatus = decision.decisionCode() == DecisionCode.RECHECK
                    ? RESULT_STATUS_REVIEW_REQUIRED
                    : RESULT_STATUS_SUCCESS;

            InspectionResult result = saveInspectionResultPort.save(InspectionResult.builder()
                    .inspectionId(runId)
                    .score(aiResponse.getScore())
                    .confidence(aiResponse.getConfidence())
                    .decisionCode(decision.decisionCode())
                    .finalDecisionCode(decision.decisionCode())
                    .resultStatus(resultStatus)
                    .thresholdSource(resolved.getSource().name())
                    .thresholdId(resolved.getThresholdId())
                    .thresholdVersion(resolved.getThresholdVersion())
                    .modelVersionId(aiResponse.getModelVersionId())
                    .build());
            eventLogger.log(runId, InspectionEventType.RESULT_SAVED, "result saved");

            boolean reviewQueued = false;
            if (decision.decisionCode() == DecisionCode.RECHECK) {
                saveReviewQueuePort.save(ReviewQueue.builder()
                        .resultId(result.getResultId())
                        .queueStatus(ReviewQueueStatus.WAITING)
                        .queuedReason(decision.queuedReason())
                        .build());
                reviewQueued = true;
            }

            runRecorder.transitTo(runId, RunStatus.COMPLETED);
            eventLogger.log(runId, InspectionEventType.COMPLETED, "completed");

            return SubmitInspectionResult.of(
                    run.toBuilder().runStatus(RunStatus.COMPLETED).build(),
                    result,
                    reviewQueued
            );

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Unexpected error during inspection submission, runId={}", runId, e);
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
