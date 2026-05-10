package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UserSettingResult;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.LoadUserSettingPort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserSetting;
import com.example.factoryguard.domain.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMySettingServiceTest {

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "session-1";

    @Mock FindUserByIdPort findUserByIdPort;
    @Mock LoadUserSettingPort loadUserSettingPort;
    @Mock SessionValidationService sessionValidationService;

    GetMySettingService service;

    @BeforeEach
    void setUp() {
        service = new GetMySettingService(findUserByIdPort, loadUserSettingPort, sessionValidationService);
        when(findUserByIdPort.findById(USER_ID)).thenReturn(Optional.of(activeUser()));
    }

    @Test
    void rowMissingReturnsDefault() {
        when(loadUserSettingPort.findByUserId(USER_ID)).thenReturn(Optional.empty());

        UserSettingResult result = service.execute(USER_ID, SESSION_ID);

        assertThat(result.getNotificationEnabled()).isTrue();
        assertThat(result.getDefaultDashboardRange()).isEqualTo("7d");
        assertThat(result.getDefaultCameraId()).isNull();
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getUserSettingId()).isNull();
    }

    @Test
    void rowPresentReturnsStoredValues() {
        UserSetting stored = UserSetting.builder()
                .userSettingId(99L)
                .userId(USER_ID)
                .notificationEnabled(false)
                .defaultDashboardRange("30d")
                .defaultCameraId(null)
                .build();
        when(loadUserSettingPort.findByUserId(USER_ID)).thenReturn(Optional.of(stored));

        UserSettingResult result = service.execute(USER_ID, SESSION_ID);

        assertThat(result.getUserSettingId()).isEqualTo(99L);
        assertThat(result.getNotificationEnabled()).isFalse();
        assertThat(result.getDefaultDashboardRange()).isEqualTo("30d");
        assertThat(result.getDefaultCameraId()).isNull();
    }

    private User activeUser() {
        return User.builder().userId(USER_ID).status(UserStatus.ACTIVE).build();
    }
}
