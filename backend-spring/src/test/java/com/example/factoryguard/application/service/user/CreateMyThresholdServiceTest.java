package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.CreateThresholdCommand;
import com.example.factoryguard.application.dto.user.UserThresholdResult;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.user.FindActiveThresholdByUserIdPort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.SaveUserThresholdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserStatus;
import com.example.factoryguard.domain.user.model.UserThreshold;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateMyThresholdServiceTest {

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "session-1";

    @Mock TokenStorePort tokenStorePort;
    @Mock FindUserByIdPort findUserByIdPort;
    @Mock FindActiveThresholdByUserIdPort findActiveThresholdByUserIdPort;
    @Mock SaveUserThresholdPort saveUserThresholdPort;

    CreateMyThresholdService service;

    @BeforeEach
    void setUp() {
        service = new CreateMyThresholdService(
                tokenStorePort, findUserByIdPort,
                findActiveThresholdByUserIdPort, saveUserThresholdPort
        );
        when(tokenStorePort.getSessionId(USER_ID)).thenReturn(Optional.of(SESSION_ID));
        when(findUserByIdPort.findById(USER_ID))
                .thenReturn(Optional.of(User.builder().userId(USER_ID).status(UserStatus.ACTIVE).build()));
    }

    @Test
    void createsThresholdAndSkipsHistorySave() {
        when(findActiveThresholdByUserIdPort.findActiveByUserId(USER_ID)).thenReturn(Optional.empty());
        when(saveUserThresholdPort.save(any(UserThreshold.class))).thenAnswer(inv -> {
            UserThreshold in = inv.getArgument(0);
            return UserThreshold.builder()
                    .thresholdId(500L)
                    .userId(in.getUserId())
                    .anomalyThreshold(in.getAnomalyThreshold())
                    .lowConfidenceThreshold(in.getLowConfidenceThreshold())
                    .minAllowed(in.getMinAllowed())
                    .maxAllowed(in.getMaxAllowed())
                    .applyScope(in.getApplyScope())
                    .isActive(in.isActive())
                    .updatedAt(LocalDateTime.now())
                    .build();
        });

        UserThresholdResult result = service.execute(USER_ID, SESSION_ID, command(0.75, 0.55));

        assertThat(result.getThresholdId()).isEqualTo(500L);
        assertThat(result.getApplyScope()).isEqualTo("DEFAULT");
        assertThat(result.getIsActive()).isTrue();
        // POST 시 USER_THRESHOLD_HISTORY 저장 생략 → thresholdVersion=null
        // (서비스에 SaveThresholdHistoryPort 의존성 자체가 없으므로 호출 가능성 없음)
        assertThat(result.getThresholdVersion()).isNull();
    }

    @Test
    void rejectsDuplicateActiveThreshold() {
        when(findActiveThresholdByUserIdPort.findActiveByUserId(USER_ID))
                .thenReturn(Optional.of(UserThreshold.builder().thresholdId(1L).build()));

        assertThatThrownBy(() -> service.execute(USER_ID, SESSION_ID, command(0.75, 0.55)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_THRESHOLD_ALREADY_EXISTS);
        verify(saveUserThresholdPort, never()).save(any(UserThreshold.class));
    }

    @Test
    void rejectsRangeViolation() {
        when(findActiveThresholdByUserIdPort.findActiveByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(USER_ID, SESSION_ID, command(1.5, 0.55)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_THRESHOLD_INVALID_RANGE);
    }

    @Test
    void rejectsRelationViolation() {
        when(findActiveThresholdByUserIdPort.findActiveByUserId(USER_ID)).thenReturn(Optional.empty());

        // lowConf >= anomaly
        assertThatThrownBy(() -> service.execute(USER_ID, SESSION_ID, command(0.55, 0.75)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_THRESHOLD_INVALID_RELATION);
    }

    private CreateThresholdCommand command(double anomaly, double lowConf) {
        return new CreateThresholdCommand(
                BigDecimal.valueOf(anomaly), BigDecimal.valueOf(lowConf), null, null
        );
    }
}
