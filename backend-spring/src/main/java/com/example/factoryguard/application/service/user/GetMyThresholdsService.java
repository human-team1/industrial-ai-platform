package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UserThresholdResult;
import com.example.factoryguard.application.port.in.user.GetMyThresholdsUseCase;
import com.example.factoryguard.application.port.out.user.FindActiveThresholdByUserIdPort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.LoadUserThresholdHistoryPort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserThreshold;
import com.example.factoryguard.domain.user.model.UserThresholdHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyThresholdsService implements GetMyThresholdsUseCase {

    private final FindUserByIdPort findUserByIdPort;
    private final FindActiveThresholdByUserIdPort findActiveThresholdByUserIdPort;
    private final LoadUserThresholdHistoryPort loadUserThresholdHistoryPort;
    private final SessionValidationService sessionValidationService;

    @Override
    public UserThresholdResult execute(Long userId, String sessionId) {
        sessionValidationService.validate(userId, sessionId);
        validateUserStatus(userId);

        UserThreshold threshold = findActiveThresholdByUserIdPort.findActiveByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_THRESHOLD_NOT_FOUND));

        Integer latestVersion = loadUserThresholdHistoryPort
                .findLatestByThresholdId(threshold.getThresholdId())
                .map(UserThresholdHistory::getVersion)
                .orElse(null);

        return UserThresholdResult.fromUserThreshold(threshold, latestVersion);
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
