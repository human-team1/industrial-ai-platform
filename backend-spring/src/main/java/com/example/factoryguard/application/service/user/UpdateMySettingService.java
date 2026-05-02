package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UpdateUserSettingCommand;
import com.example.factoryguard.application.dto.user.UserSettingResult;
import com.example.factoryguard.application.port.in.user.UpdateUserSettingUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.SaveUserSettingPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateMySettingService implements UpdateUserSettingUseCase {

    private final TokenStorePort tokenStorePort;
    private final FindUserByIdPort findUserByIdPort;
    private final SaveUserSettingPort saveUserSettingPort;

    @Override
    public UserSettingResult execute(UpdateUserSettingCommand command) {
        Long userId = command.getUserId();
        validateSession(userId, command.getSessionId());
        validateUserStatus(userId);
        validateDefaultCameraId(command.getDefaultCameraId());

        UserSetting incoming = UserSetting.builder()
                .userId(userId)
                .notificationEnabled(command.getNotificationEnabled() != null ? command.getNotificationEnabled() : UserSettingResult.DEFAULT_NOTIFICATION_ENABLED)
                .defaultDashboardRange(command.getDefaultDashboardRange() != null ? command.getDefaultDashboardRange() : UserSettingResult.DEFAULT_DASHBOARD_RANGE)
                .defaultCameraId(command.getDefaultCameraId())
                .build();

        UserSetting saved;
        try {
            saved = saveUserSettingPort.save(incoming);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.USER_SETTING_SAVE_FAILED);
        }
        return UserSettingResult.fromUserSetting(saved);
    }

    /**
     * MVP 정책: 카메라 목록 연동 전이므로 defaultCameraId는 null만 허용한다.
     * non-null 요청은 422로 차단한다. 추후 카메라 조회 포트와 organizationId 검증이
     * 정합화되면 비-null 저장을 지원한다.
     */
    private void validateDefaultCameraId(Long defaultCameraId) {
        if (defaultCameraId != null) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_FAILED,
                    "defaultCameraId 설정은 아직 지원하지 않습니다."
            );
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
