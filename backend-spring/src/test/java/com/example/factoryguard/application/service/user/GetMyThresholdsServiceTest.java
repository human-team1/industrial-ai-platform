package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UserThresholdResult;
import com.example.factoryguard.application.port.out.user.FindActiveThresholdByUserIdPort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.LoadUserThresholdHistoryPort;
import com.example.factoryguard.application.service.auth.SessionValidationService;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserStatus;
import com.example.factoryguard.domain.user.model.UserThreshold;
import com.example.factoryguard.domain.user.model.UserThresholdHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyThresholdsServiceTest {

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "session-1";

    @Mock FindUserByIdPort findUserByIdPort;
    @Mock FindActiveThresholdByUserIdPort findActiveThresholdByUserIdPort;
    @Mock LoadUserThresholdHistoryPort loadUserThresholdHistoryPort;
    @Mock SessionValidationService sessionValidationService;

    GetMyThresholdsService service;

    @BeforeEach
    void setUp() {
        service = new GetMyThresholdsService(
                findUserByIdPort,
                findActiveThresholdByUserIdPort,
                loadUserThresholdHistoryPort,
                sessionValidationService
        );
        when(findUserByIdPort.findById(USER_ID))
                .thenReturn(Optional.of(User.builder().userId(USER_ID).status(UserStatus.ACTIVE).build()));
    }

    @Test
    void activeMissingReturnsSystemDefaultThreshold() {
        when(findActiveThresholdByUserIdPort.findActiveByUserId(USER_ID)).thenReturn(Optional.empty());

        UserThresholdResult result = service.execute(USER_ID, SESSION_ID);

        assertThat(result.getThresholdId()).isNull();
        assertThat(result.getAnomalyThreshold()).isEqualByComparingTo(BigDecimal.valueOf(0.75));
        assertThat(result.getLowConfidenceThreshold()).isEqualByComparingTo(BigDecimal.valueOf(0.55));
        assertThat(result.getApplyScope()).isEqualTo("SYSTEM_DEFAULT");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void activePresentReturnsSingleObjectWithLatestVersion() {
        UserThreshold threshold = userThreshold();
        when(findActiveThresholdByUserIdPort.findActiveByUserId(USER_ID)).thenReturn(Optional.of(threshold));
        when(loadUserThresholdHistoryPort.findLatestByThresholdId(100L)).thenReturn(Optional.of(history(3)));

        UserThresholdResult result = service.execute(USER_ID, SESSION_ID);

        assertThat(result.getThresholdId()).isEqualTo(100L);
        assertThat(result.getAnomalyThreshold()).isEqualByComparingTo(BigDecimal.valueOf(0.75));
        assertThat(result.getLowConfidenceThreshold()).isEqualByComparingTo(BigDecimal.valueOf(0.55));
        assertThat(result.getApplyScope()).isEqualTo("DEFAULT");
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getThresholdVersion()).isEqualTo(3);
    }

    @Test
    void historyEmptyResultsInNullThresholdVersion() {
        when(findActiveThresholdByUserIdPort.findActiveByUserId(USER_ID)).thenReturn(Optional.of(userThreshold()));
        when(loadUserThresholdHistoryPort.findLatestByThresholdId(100L)).thenReturn(Optional.empty());

        UserThresholdResult result = service.execute(USER_ID, SESSION_ID);

        assertThat(result.getThresholdVersion()).isNull();
    }

    private UserThreshold userThreshold() {
        return UserThreshold.builder()
                .thresholdId(100L)
                .userId(USER_ID)
                .anomalyThreshold(0.75)
                .lowConfidenceThreshold(0.55)
                .minAllowed(0.0)
                .maxAllowed(1.0)
                .applyScope("DEFAULT")
                .isActive(true)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UserThresholdHistory history(int version) {
        return UserThresholdHistory.builder()
                .thresholdId(100L)
                .version(version)
                .oldAnomalyThreshold(0.7)
                .newAnomalyThreshold(0.75)
                .changeReason("USER_SETTING_PAGE_UPDATE")
                .changedBy(USER_ID)
                .changedAt(LocalDateTime.now())
                .build();
    }
}
