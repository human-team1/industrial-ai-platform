package com.example.factoryguard.application.service.auth;

import com.example.factoryguard.application.dto.auth.AuthMeResult;
import com.example.factoryguard.application.port.in.auth.GetAuthMeUseCase;
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
public class GetAuthMeService implements GetAuthMeUseCase {

    private final FindUserByIdPort findUserByIdPort;
    private final SessionValidationService sessionValidationService;
    private final ActiveSessionService activeSessionService;

    @Override
    public AuthMeResult execute(Long userId, String sessionId) {
        sessionValidationService.validate(userId, sessionId);
        activeSessionService.refreshSession(sessionId);

        User user = findUserByIdPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        return switch (user.getStatus()) {
            case ACTIVE -> AuthMeResult.from(user);
            case PENDING -> throw new BusinessException(ErrorCode.PENDING_APPROVAL);
            case REJECTED -> throw new BusinessException(ErrorCode.ACCOUNT_REJECTED);
            case INACTIVE -> throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
        };
    }
}
