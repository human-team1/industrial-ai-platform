package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UserThresholdResult;
import com.example.factoryguard.application.port.in.user.GetMyThresholdsUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.user.FindActiveThresholdByUserIdPort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyThresholdsService implements GetMyThresholdsUseCase {

    private final TokenStorePort tokenStorePort;
    private final FindUserByIdPort findUserByIdPort;
    private final FindActiveThresholdByUserIdPort findActiveThresholdByUserIdPort;

    @Override
    public UserThresholdResult execute(Long userId, String sessionId) {
        validateSession(userId, sessionId);
        validateUserStatus(userId);

        return findActiveThresholdByUserIdPort.findActiveByUserId(userId)
                .map(UserThresholdResult::fromUserThreshold)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_THRESHOLD_NOT_FOUND));
    }

    private void validateSession(Long userId, String sessionId) {
        if (sessionId == null) {
            throw new BusinessException(ErrorCode.SESSION_INVALID);
        }

        String currentSessionId = tokenStorePort.getSessionId(userId).orElse(null);

        if (currentSessionId == null || currentSessionId.isBlank()) {
            throw new BusinessException(ErrorCode.SESSION_INVALID);
        }

        if (!currentSessionId.equals(sessionId)) {
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