package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UpdateThresholdCommand;
import com.example.factoryguard.application.dto.user.UpdateThresholdResult;
import com.example.factoryguard.application.port.in.user.UpdateMyThresholdUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.LoadThresholdByIdPort;
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

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateMyThresholdService implements UpdateMyThresholdUseCase {

    private final TokenStorePort tokenStorePort;
    private final FindUserByIdPort findUserByIdPort;
    private final LoadThresholdByIdPort loadThresholdByIdPort;
    private final UpdateThresholdPort updateThresholdPort;
    private final SaveThresholdHistoryPort saveThresholdHistoryPort;

    @Override
    public UpdateThresholdResult execute(Long userId, String sessionId, Long thresholdId, UpdateThresholdCommand command) {
        validateSession(userId, sessionId);
        User user = validateUserStatus(userId);

        UserThreshold existing = loadThresholdByIdPort.findById(thresholdId)
                .orElseThrow(() -> new BusinessException(ErrorCode.THRESHOLD_NOT_FOUND));

        if (!existing.isActive()) {
            throw new BusinessException(ErrorCode.THRESHOLD_NOT_FOUND);
        }

        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        validateRange(command.getAnomalyThreshold(), existing.getMinAllowed(), existing.getMaxAllowed());
        validateRange(command.getLowConfidenceThreshold(), existing.getMinAllowed(), existing.getMaxAllowed());

        UserThreshold updated = UserThreshold.builder()
                .thresholdId(existing.getThresholdId())
                .userId(existing.getUserId())
                .anomalyThreshold(command.getAnomalyThreshold())
                .lowConfidenceThreshold(command.getLowConfidenceThreshold())
                .minAllowed(existing.getMinAllowed())
                .maxAllowed(existing.getMaxAllowed())
                .applyScope(command.getApplyScope())
                .isActive(existing.isActive())
                .createdAt(existing.getCreatedAt())
                .build();

        UserThreshold saved = updateThresholdPort.update(updated);

        saveThresholdHistoryPort.save(UserThresholdHistory.builder()
                .thresholdId(existing.getThresholdId())
                .oldAnomalyThreshold(existing.getAnomalyThreshold())
                .newAnomalyThreshold(command.getAnomalyThreshold())
                .changeReason(command.getChangeReason())
                .changedBy(userId)
                .build());

        return UpdateThresholdResult.from(saved);
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

    private void validateRange(double value, double min, double max) {
        if (value < min || value > max) {
            throw new BusinessException(ErrorCode.THRESHOLD_OUT_OF_RANGE);
        }
    }
}