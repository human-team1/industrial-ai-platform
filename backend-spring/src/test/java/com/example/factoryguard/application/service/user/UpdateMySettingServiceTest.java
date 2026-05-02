package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UpdateUserSettingCommand;
import com.example.factoryguard.application.dto.user.UserSettingResult;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.SaveUserSettingPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserSetting;
import com.example.factoryguard.domain.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateMySettingServiceTest {

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "session-1";

    @Mock TokenStorePort tokenStorePort;
    @Mock FindUserByIdPort findUserByIdPort;
    @Mock SaveUserSettingPort saveUserSettingPort;

    UpdateMySettingService service;

    @BeforeEach
    void setUp() {
        service = new UpdateMySettingService(tokenStorePort, findUserByIdPort, saveUserSettingPort);
    }

    @Test
    void savesUpsertWithDefaultCameraIdNull() {
        prepareSession();
        UpdateUserSettingCommand command = new UpdateUserSettingCommand(
                USER_ID, SESSION_ID, true, "30d", null
        );
        when(saveUserSettingPort.save(any(UserSetting.class))).thenAnswer(inv -> {
            UserSetting in = inv.getArgument(0);
            return UserSetting.builder()
                    .userSettingId(10L)
                    .userId(in.getUserId())
                    .notificationEnabled(in.getNotificationEnabled())
                    .defaultDashboardRange(in.getDefaultDashboardRange())
                    .defaultCameraId(in.getDefaultCameraId())
                    .build();
        });

        UserSettingResult result = service.execute(command);

        ArgumentCaptor<UserSetting> captor = ArgumentCaptor.forClass(UserSetting.class);
        verify(saveUserSettingPort).save(captor.capture());
        UserSetting saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getNotificationEnabled()).isTrue();
        assertThat(saved.getDefaultDashboardRange()).isEqualTo("30d");
        assertThat(saved.getDefaultCameraId()).isNull();
        assertThat(result.getUserSettingId()).isEqualTo(10L);
    }

    @Test
    void rejectsNonNullDefaultCameraIdAsValidationFailed() {
        prepareSession();
        UpdateUserSettingCommand command = new UpdateUserSettingCommand(
                USER_ID, SESSION_ID, true, "7d", 99L
        );

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);
        verify(saveUserSettingPort, never()).save(any(UserSetting.class));
    }

    @Test
    void wrapsSaveFailureWithUserSettingSaveFailed() {
        prepareSession();
        UpdateUserSettingCommand command = new UpdateUserSettingCommand(
                USER_ID, SESSION_ID, true, "7d", null
        );
        when(saveUserSettingPort.save(any(UserSetting.class)))
                .thenThrow(new RuntimeException("DB down"));

        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_SETTING_SAVE_FAILED);
    }

    private void prepareSession() {
        when(tokenStorePort.getSessionId(USER_ID)).thenReturn(Optional.of(SESSION_ID));
        when(findUserByIdPort.findById(USER_ID))
                .thenReturn(Optional.of(User.builder().userId(USER_ID).status(UserStatus.ACTIVE).build()));
    }
}
