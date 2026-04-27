package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UserMeResult;
import com.example.factoryguard.application.port.in.user.GetMyProfileUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
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
public class GetMyProfileService implements GetMyProfileUseCase {

    private final TokenStorePort tokenStorePort;
    private final FindUserByIdPort findUserByIdPort;

    @Override
    public UserMeResult execute(Long userId, String sessionId) {
        validateSession(userId, sessionId);

        User user = findUserByIdPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        return switch (user.getStatus()) {
            case ACTIVE -> UserMeResult.from(user);
            case PENDING -> throw new BusinessException(ErrorCode.PENDING_APPROVAL);
            case REJECTED -> throw new BusinessException(ErrorCode.ACCOUNT_REJECTED);
            default -> throw new BusinessException(ErrorCode.UNAUTHORIZED);
        };
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
}