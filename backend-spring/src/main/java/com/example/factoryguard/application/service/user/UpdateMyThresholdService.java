package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UpdateThresholdCommand;
import com.example.factoryguard.application.dto.user.UserThresholdResult;
import com.example.factoryguard.application.port.in.user.UpdateMyThresholdUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.LoadThresholdByIdPort;
import com.example.factoryguard.application.port.out.user.LoadUserThresholdHistoryPort;
import com.example.factoryguard.application.port.out.user.SaveThresholdHistoryPort;
import com.example.factoryguard.application.port.out.user.UpdateThresholdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserThreshold;
import com.example.factoryguard.domain.user.model.UserThresholdHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateMyThresholdService implements UpdateMyThresholdUseCase {

    private static final String DEFAULT_CHANGE_REASON = "USER_SETTING_PAGE_UPDATE";
    private static final String DEFAULT_APPLY_SCOPE = "DEFAULT";

    private final TokenStorePort tokenStorePort;
    private final FindUserByIdPort findUserByIdPort;
    private final LoadThresholdByIdPort loadThresholdByIdPort;
    private final UpdateThresholdPort updateThresholdPort;
    private final SaveThresholdHistoryPort saveThresholdHistoryPort;
    private final LoadUserThresholdHistoryPort loadUserThresholdHistoryPort;

    @Override
    public UserThresholdResult execute(Long userId, String sessionId, Long thresholdId, UpdateThresholdCommand command) {
        validateSession(userId, sessionId);
        validateUserStatus(userId);

        // 1. 존재
        UserThreshold existing = loadThresholdByIdPort.findById(thresholdId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_THRESHOLD_NOT_FOUND));

        // 2. 소유자
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_THRESHOLD_FORBIDDEN);
        }

        // 3. active
        if (!existing.isActive()) {
            throw new BusinessException(ErrorCode.USER_THRESHOLD_INACTIVE);
        }

        // 4. lowConfidenceThreshold 변경 제한 (BigDecimal.compareTo 기준)
        // DB scale 차이로 0.55와 0.5500이 다르게 처리되지 않도록 compareTo 사용 (equals 금지)
        BigDecimal existingLowConf = BigDecimal.valueOf(existing.getLowConfidenceThreshold());
        if (command.getLowConfidenceThreshold().compareTo(existingLowConf) != 0) {
            throw new BusinessException(ErrorCode.USER_THRESHOLD_LOW_CONFIDENCE_CHANGE_UNSUPPORTED);
        }

        // 5. range 검증 (DB의 minAllowed/maxAllowed 기준)
        BigDecimal min = BigDecimal.valueOf(existing.getMinAllowed());
        BigDecimal max = BigDecimal.valueOf(existing.getMaxAllowed());
        validateRange(command.getAnomalyThreshold(), min, max);
        validateRange(command.getLowConfidenceThreshold(), min, max);

        // 6. relation 검증 (lowConf < anomaly)
        validateRelation(command.getAnomalyThreshold(), command.getLowConfidenceThreshold());

        // 7. changeReason 기본값
        String changeReason = (command.getChangeReason() == null || command.getChangeReason().isBlank())
                ? DEFAULT_CHANGE_REASON
                : command.getChangeReason();
        String applyScope = (command.getApplyScope() == null || command.getApplyScope().isBlank())
                ? (existing.getApplyScope() == null ? DEFAULT_APPLY_SCOPE : existing.getApplyScope())
                : command.getApplyScope();

        // 8. update + history 저장 (트랜잭션)
        UserThreshold updated = UserThreshold.builder()
                .thresholdId(existing.getThresholdId())
                .userId(existing.getUserId())
                .anomalyThreshold(command.getAnomalyThreshold().doubleValue())
                .lowConfidenceThreshold(command.getLowConfidenceThreshold().doubleValue())
                .minAllowed(existing.getMinAllowed())
                .maxAllowed(existing.getMaxAllowed())
                .applyScope(applyScope)
                .isActive(existing.isActive())
                .createdAt(existing.getCreatedAt())
                .build();

        UserThreshold saved = updateThresholdPort.update(updated);

        // history version: 기존 max(version)+1, POST에서 history 저장을 생략했으므로 첫 PATCH 시 1
        Integer nextVersion = loadUserThresholdHistoryPort.findLatestByThresholdId(thresholdId)
                .map(h -> h.getVersion() == null ? 1 : h.getVersion() + 1)
                .orElse(1);

        saveThresholdHistoryPort.save(UserThresholdHistory.builder()
                .thresholdId(existing.getThresholdId())
                .version(nextVersion)
                .oldAnomalyThreshold(existing.getAnomalyThreshold())
                .newAnomalyThreshold(command.getAnomalyThreshold().doubleValue())
                .changeReason(changeReason)
                .changedBy(userId)
                .build());

        return UserThresholdResult.fromUserThreshold(saved, nextVersion);
    }

    private void validateRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new BusinessException(ErrorCode.USER_THRESHOLD_INVALID_RANGE);
        }
    }

    private void validateRelation(BigDecimal anomaly, BigDecimal lowConfidence) {
        if (lowConfidence.compareTo(anomaly) >= 0) {
            throw new BusinessException(ErrorCode.USER_THRESHOLD_INVALID_RELATION);
        }
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

    private void validateUserStatus(Long userId) {
        User user = findUserByIdPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        switch (user.getStatus()) {
            case ACTIVE -> { /* 정상 진행 */ }
            case PENDING -> throw new BusinessException(ErrorCode.PENDING_APPROVAL);
            case REJECTED -> throw new BusinessException(ErrorCode.ACCOUNT_REJECTED);
            default -> throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
