package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.CreateThresholdCommand;
import com.example.factoryguard.application.dto.user.UserThresholdResult;
import com.example.factoryguard.application.port.in.user.CreateMyThresholdUseCase;
import com.example.factoryguard.application.port.out.user.FindActiveThresholdByUserIdPort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.SaveUserThresholdPort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserThreshold;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateMyThresholdService implements CreateMyThresholdUseCase {

    private static final String DEFAULT_APPLY_SCOPE = "DEFAULT";
    private static final BigDecimal DEFAULT_MIN_ALLOWED = new BigDecimal("0.0000");
    private static final BigDecimal DEFAULT_MAX_ALLOWED = new BigDecimal("1.0000");

    private final FindUserByIdPort findUserByIdPort;
    private final FindActiveThresholdByUserIdPort findActiveThresholdByUserIdPort;
    private final SaveUserThresholdPort saveUserThresholdPort;
    private final SessionValidationService sessionValidationService;

    @Override
    public UserThresholdResult execute(Long userId, String sessionId, CreateThresholdCommand command) {
        sessionValidationService.validate(userId, sessionId);
        validateUserStatus(userId);

        if (findActiveThresholdByUserIdPort.findActiveByUserId(userId).isPresent()) {
            throw new BusinessException(ErrorCode.USER_THRESHOLD_ALREADY_EXISTS);
        }

        BigDecimal minAllowed = DEFAULT_MIN_ALLOWED;
        BigDecimal maxAllowed = DEFAULT_MAX_ALLOWED;
        validateRange(command.getAnomalyThreshold(), minAllowed, maxAllowed);
        validateRange(command.getLowConfidenceThreshold(), minAllowed, maxAllowed);
        validateRelation(command.getAnomalyThreshold(), command.getLowConfidenceThreshold());

        String applyScope = (command.getApplyScope() == null || command.getApplyScope().isBlank())
                ? DEFAULT_APPLY_SCOPE
                : command.getApplyScope();

        // BigDecimal -> double 변환은 정밀도 손실 위험이 있음 (USER_THRESHOLD 컬럼이 현재 double).
        // DECIMAL(5,4)/BigDecimal로 정합화되면 이 변환은 제거 대상.
        UserThreshold incoming = UserThreshold.builder()
                .userId(userId)
                .anomalyThreshold(command.getAnomalyThreshold().doubleValue())
                .lowConfidenceThreshold(command.getLowConfidenceThreshold().doubleValue())
                .minAllowed(minAllowed.doubleValue())
                .maxAllowed(maxAllowed.doubleValue())
                .applyScope(applyScope)
                .isActive(true)
                .build();

        UserThreshold saved = saveUserThresholdPort.save(incoming);

        // POST 생성 시 USER_THRESHOLD_HISTORY.old_anomaly_threshold가 NOT NULL 제약으로 저장 생략.
        // 첫 PATCH 시 version=1로 시작. 응답 thresholdVersion=null.
        return UserThresholdResult.fromUserThreshold(saved, null);
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
