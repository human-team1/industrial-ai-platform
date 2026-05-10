package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UserSettingResult;
import com.example.factoryguard.application.port.in.user.GetUserSettingUseCase;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.LoadUserSettingPort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMySettingService implements GetUserSettingUseCase {

    private final FindUserByIdPort findUserByIdPort;
    private final LoadUserSettingPort loadUserSettingPort;
    private final SessionValidationService sessionValidationService;

    @Override
    public UserSettingResult execute(Long userId, String sessionId) {
        sessionValidationService.validate(userId, sessionId);
        validateUserStatus(userId);

        return loadUserSettingPort.findByUserId(userId)
                .map(UserSettingResult::fromUserSetting)
                .orElseGet(() -> UserSettingResult.defaultFor(userId));
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
