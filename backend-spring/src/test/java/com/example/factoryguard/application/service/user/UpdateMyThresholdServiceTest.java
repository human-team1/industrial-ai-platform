package com.example.factoryguard.application.service.user;

import com.example.factoryguard.application.dto.user.UpdateThresholdCommand;
import com.example.factoryguard.application.dto.user.UserThresholdResult;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.application.port.out.user.LoadThresholdByIdPort;
import com.example.factoryguard.application.port.out.user.LoadUserThresholdHistoryPort;
import com.example.factoryguard.application.port.out.user.SaveThresholdHistoryPort;
import com.example.factoryguard.application.port.out.user.UpdateThresholdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserStatus;
import com.example.factoryguard.domain.user.model.UserThreshold;
import com.example.factoryguard.domain.user.model.UserThresholdHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UpdateMyThresholdServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long THRESHOLD_ID = 100L;
    private static final String SESSION_ID = "session-1";

    @Mock TokenStorePort tokenStorePort;
    @Mock FindUserByIdPort findUserByIdPort;
    @Mock LoadThresholdByIdPort loadThresholdByIdPort;
    @Mock UpdateThresholdPort updateThresholdPort;
    @Mock SaveThresholdHistoryPort saveThresholdHistoryPort;
    @Mock LoadUserThresholdHistoryPort loadUserThresholdHistoryPort;

    UpdateMyThresholdService service;

    @BeforeEach
    void setUp() {
        service = new UpdateMyThresholdService(
                tokenStorePort, findUserByIdPort, loadThresholdByIdPort,
                updateThresholdPort, saveThresholdHistoryPort, loadUserThresholdHistoryPort
        );
        when(tokenStorePort.getSessionId(USER_ID)).thenReturn(Optional.of(SESSION_ID));
        when(findUserByIdPort.findById(USER_ID))
                .thenReturn(Optional.of(User.builder().userId(USER_ID).status(UserStatus.ACTIVE).build()));
    }

    @Test
    void firstPatchSavesHistoryWithVersionOne() {
        UserThreshold existing = activeThreshold(0.75, 0.55);
        when(loadThresholdByIdPort.findById(THRESHOLD_ID)).thenReturn(Optional.of(existing));
        when(loadUserThresholdHistoryPort.findLatestByThresholdId(THRESHOLD_ID)).thenReturn(Optional.empty());
        when(updateThresholdPort.update(any(UserThreshold.class))).thenAnswer(inv -> inv.getArgument(0));

        UserThresholdResult result = service.execute(USER_ID, SESSION_ID, THRESHOLD_ID, command(0.80, 0.55, null));

        ArgumentCaptor<UserThresholdHistory> captor = ArgumentCaptor.forClass(UserThresholdHistory.class);
        verify(saveThresholdHistoryPort).save(captor.capture());
        UserThresholdHistory history = captor.getValue();
        assertThat(history.getVersion()).isEqualTo(1);
        assertThat(history.getOldAnomalyThreshold()).isEqualTo(0.75);
        assertThat(history.getNewAnomalyThreshold()).isEqualTo(0.80);
        assertThat(history.getChangeReason()).isEqualTo("USER_SETTING_PAGE_UPDATE");
        assertThat(history.getChangedBy()).isEqualTo(USER_ID);
        assertThat(result.getThresholdVersion()).isEqualTo(1);
    }

    @Test
    void subsequentPatchIncrementsVersion() {
        UserThreshold existing = activeThreshold(0.75, 0.55);
        when(loadThresholdByIdPort.findById(THRESHOLD_ID)).thenReturn(Optional.of(existing));
        when(loadUserThresholdHistoryPort.findLatestByThresholdId(THRESHOLD_ID))
                .thenReturn(Optional.of(historyWithVersion(3)));
        when(updateThresholdPort.update(any(UserThreshold.class))).thenAnswer(inv -> inv.getArgument(0));

        UserThresholdResult result = service.execute(USER_ID, SESSION_ID, THRESHOLD_ID, command(0.80, 0.55, null));

        ArgumentCaptor<UserThresholdHistory> captor = ArgumentCaptor.forClass(UserThresholdHistory.class);
        verify(saveThresholdHistoryPort).save(captor.capture());
        assertThat(captor.getValue().getVersion()).isEqualTo(4);
        assertThat(result.getThresholdVersion()).isEqualTo(4);
    }

    @Test
    void missingThresholdThrowsNotFound() {
        when(loadThresholdByIdPort.findById(THRESHOLD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(USER_ID, SESSION_ID, THRESHOLD_ID, command(0.8, 0.55, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_THRESHOLD_NOT_FOUND);
    }

    @Test
    void otherUserThresholdThrowsForbidden() {
        UserThreshold otherOwned = UserThreshold.builder()
                .thresholdId(THRESHOLD_ID).userId(OTHER_USER_ID)
                .anomalyThreshold(0.75).lowConfidenceThreshold(0.55)
                .minAllowed(0.0).maxAllowed(1.0).applyScope("DEFAULT").isActive(true).build();
        when(loadThresholdByIdPort.findById(THRESHOLD_ID)).thenReturn(Optional.of(otherOwned));

        assertThatThrownBy(() -> service.execute(USER_ID, SESSION_ID, THRESHOLD_ID, command(0.8, 0.55, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_THRESHOLD_FORBIDDEN);
    }

    @Test
    void inactiveThresholdThrowsConflict() {
        UserThreshold inactive = UserThreshold.builder()
                .thresholdId(THRESHOLD_ID).userId(USER_ID)
                .anomalyThreshold(0.75).lowConfidenceThreshold(0.55)
                .minAllowed(0.0).maxAllowed(1.0).applyScope("DEFAULT").isActive(false).build();
        when(loadThresholdByIdPort.findById(THRESHOLD_ID)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> service.execute(USER_ID, SESSION_ID, THRESHOLD_ID, command(0.8, 0.55, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_THRESHOLD_INACTIVE);
    }

    @Test
    void rangeViolationThrowsInvalidRange() {
        when(loadThresholdByIdPort.findById(THRESHOLD_ID)).thenReturn(Optional.of(activeThreshold(0.75, 0.55)));

        assertThatThrownBy(() -> service.execute(USER_ID, SESSION_ID, THRESHOLD_ID, command(1.2, 0.55, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_THRESHOLD_INVALID_RANGE);
    }

    @Test
    void relationViolationThrowsInvalidRelation() {
        when(loadThresholdByIdPort.findById(THRESHOLD_ID)).thenReturn(Optional.of(activeThreshold(0.75, 0.55)));
        // anomaly=0.50, lowConf=0.55 → relation 위반. 단 lowConf 변경(0.55→0.55) 없음.

        assertThatThrownBy(() -> service.execute(USER_ID, SESSION_ID, THRESHOLD_ID, command(0.50, 0.55, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_THRESHOLD_INVALID_RELATION);
    }

    @Test
    void scaleDifferentLowConfPassesAsUnchanged() {
        // 기존 lowConf=0.5500, 요청 lowConf=0.55 → compareTo=0이므로 변경으로 보지 않고 통과
        UserThreshold existing = activeThresholdScaled(0.75, "0.5500");
        when(loadThresholdByIdPort.findById(THRESHOLD_ID)).thenReturn(Optional.of(existing));
        when(loadUserThresholdHistoryPort.findLatestByThresholdId(THRESHOLD_ID)).thenReturn(Optional.empty());
        when(updateThresholdPort.update(any(UserThreshold.class))).thenAnswer(inv -> inv.getArgument(0));

        UserThresholdResult result = service.execute(USER_ID, SESSION_ID, THRESHOLD_ID,
                new UpdateThresholdCommand(new BigDecimal("0.80"), new BigDecimal("0.55"), null, null));

        assertThat(result.getThresholdVersion()).isEqualTo(1);
    }

    @Test
    void changedLowConfThrowsLowConfidenceChangeUnsupported() {
        when(loadThresholdByIdPort.findById(THRESHOLD_ID)).thenReturn(Optional.of(activeThreshold(0.75, 0.55)));

        assertThatThrownBy(() -> service.execute(USER_ID, SESSION_ID, THRESHOLD_ID, command(0.8, 0.60, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_THRESHOLD_LOW_CONFIDENCE_CHANGE_UNSUPPORTED);
        verify(updateThresholdPort, never()).update(any(UserThreshold.class));
    }

    @Test
    void lowConfidenceCheckPrecedesRelationCheck() {
        // 기존 lowConf=0.55, 요청 lowConf=0.80, anomaly=0.75 (relation도 위반이지만 lowConf 변경이 먼저)
        when(loadThresholdByIdPort.findById(THRESHOLD_ID)).thenReturn(Optional.of(activeThreshold(0.75, 0.55)));

        assertThatThrownBy(() -> service.execute(USER_ID, SESSION_ID, THRESHOLD_ID, command(0.75, 0.80, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.USER_THRESHOLD_LOW_CONFIDENCE_CHANGE_UNSUPPORTED);
    }

    private UpdateThresholdCommand command(double anomaly, double lowConf, String changeReason) {
        return new UpdateThresholdCommand(
                BigDecimal.valueOf(anomaly), BigDecimal.valueOf(lowConf), null, changeReason
        );
    }

    private UserThreshold activeThreshold(double anomaly, double lowConf) {
        return UserThreshold.builder()
                .thresholdId(THRESHOLD_ID)
                .userId(USER_ID)
                .anomalyThreshold(anomaly)
                .lowConfidenceThreshold(lowConf)
                .minAllowed(0.0)
                .maxAllowed(1.0)
                .applyScope("DEFAULT")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UserThreshold activeThresholdScaled(double anomaly, String lowConfString) {
        return UserThreshold.builder()
                .thresholdId(THRESHOLD_ID)
                .userId(USER_ID)
                .anomalyThreshold(anomaly)
                .lowConfidenceThreshold(new BigDecimal(lowConfString).doubleValue())
                .minAllowed(0.0)
                .maxAllowed(1.0)
                .applyScope("DEFAULT")
                .isActive(true)
                .build();
    }

    private UserThresholdHistory historyWithVersion(int version) {
        return UserThresholdHistory.builder()
                .thresholdId(THRESHOLD_ID)
                .version(version)
                .oldAnomalyThreshold(0.7)
                .newAnomalyThreshold(0.75)
                .changeReason("USER_SETTING_PAGE_UPDATE")
                .changedBy(USER_ID)
                .changedAt(LocalDateTime.now())
                .build();
    }
}
