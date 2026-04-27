package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.*;
import com.example.factoryguard.application.port.in.inspection.SubmitInspectionUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.inspection.*;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.model.*;
import com.example.factoryguard.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SubmitInspectionService implements SubmitInspectionUseCase {

    private final TokenStorePort tokenStorePort;
    private final FindUserByIdPort findUserByIdPort;
    private final LoadAnalysisTargetPort loadAnalysisTargetPort;
    private final SaveInspectionRunPort saveInspectionRunPort;
    private final SaveInspectionResultPort saveInspectionResultPort;
    private final CallAiInspectionPort callAiInspectionPort;
    private final ResolveInspectionThresholdService resolveInspectionThresholdService;
    private final InspectionRunRecorder inspectionRunRecorder;

    @Override
    public SubmitInspectionResult execute(SubmitInspectionCommand command) {
        // ① 인증 검증
        validateSession(command.getUserId(), command.getSessionId());
        User user = validateUserStatus(command.getUserId());

        // ② threshold 해소 (thresholdId 소유권 검증 포함)
        ResolvedThreshold resolved = resolveInspectionThresholdService.resolve(
                command.getUserId(), command.getThresholdId());

        // ③ target 조회
        AnalysisTarget target = loadAnalysisTargetPort.findById(command.getTargetId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND));

        // ④ 조직 검증 — 멀티 테넌시 보호
        if (!target.getOrganizationId().equals(user.getOrganizationId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // ⑤ INSPECTION_RUN 생성 — 별도 트랜잭션으로 즉시 커밋
        InspectionRun run = inspectionRunRecorder.record(InspectionRun.builder()
                .organizationId(user.getOrganizationId())
                .userId(command.getUserId())
                .targetId(command.getTargetId())
                .runType(RunType.UPLOAD)
                .inputType("FILE")
                .sourceType("UPLOAD")
                .sourceId(command.getOriginalFileName())
                .runStatus(RunStatus.RUNNING)
                .appliedThreshold(resolved.getAnomalyThreshold())
                .idempotencyKey(UUID.randomUUID().toString())
                .startedAt(LocalDateTime.now())
                .build());

        // ⑥ FastAPI 호출
        AiInspectionResponse aiResponse;
        try {
            aiResponse = callAiInspectionPort.call(new AiInspectionRequest(
                    command.getFileUrl(),
                    resolved.getAnomalyThreshold(),
                    resolved.getLowConfidenceThreshold()
            ));
        } catch (Exception e) {
            inspectionRunRecorder.markFailed(run);  // AI 실패 시 FAILED 상태로 기록
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }

        // ⑦ 판단 적용
        DecisionCode decision = DecisionCalculator.decide(
                aiResponse.getScore(),
                aiResponse.getConfidence(),
                resolved.getAnomalyThreshold(),
                resolved.getLowConfidenceThreshold()
        );

        // ⑧ INSPECTION_RESULT 저장
        InspectionResult result = saveInspectionResultPort.save(InspectionResult.builder()
                .inspectionId(run.getInspectionId())
                .score(aiResponse.getScore())
                .confidence(aiResponse.getConfidence())
                .decisionCode(decision)
                .finalDecisionCode(decision)
                .resultStatus("COMPLETED")
                .thresholdSource(resolved.getSource().name())
                .thresholdId(resolved.getThresholdId())
                .build());

        return SubmitInspectionResult.of(run, result);
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