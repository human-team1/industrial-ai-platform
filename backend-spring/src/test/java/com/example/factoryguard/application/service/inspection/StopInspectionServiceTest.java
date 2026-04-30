package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.model.RunType;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StopInspectionServiceTest {

    @Mock LoadInspectionRunPort loadInspectionRunPort;
    @Mock InspectionRunRecorder runRecorder;
    @Mock InspectionEventLogger eventLogger;

    StopInspectionService service;

    @BeforeEach
    void setUp() {
        service = new StopInspectionService(loadInspectionRunPort, runRecorder, eventLogger);
    }

    @Test
    void stopRealtimeInspectionSucceeds() {
        InspectionRun processing = run(RunStatus.PROCESSING, null);
        InspectionRun stopped = run(RunStatus.STOPPED, LocalDateTime.now());
        when(loadInspectionRunPort.findRunById(1001L))
                .thenReturn(Optional.of(processing))
                .thenReturn(Optional.of(stopped));

        InspectionRun result = service.execute(1L, 10L, 1001L);

        assertThat(result.getRunStatus()).isEqualTo(RunStatus.STOPPED);
        assertThat(result.getCompletedAt()).isNotNull();
        verify(runRecorder).transitTo(1001L, RunStatus.STOPPED);
        verify(eventLogger).log(1001L, InspectionEventType.REALTIME_STOPPED, "실시간 탐지가 중지되었습니다.");
    }

    @Test
    void stopOtherOrganizationRunThrowsForbidden() {
        when(loadInspectionRunPort.findRunById(1001L)).thenReturn(Optional.of(run(99L, RunStatus.PROCESSING)));

        assertThatThrownBy(() -> service.execute(1L, 10L, 1001L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getDefaultMessage());
    }

    @Test
    void stopAlreadyFinishedRunThrowsConflict() {
        when(loadInspectionRunPort.findRunById(1001L)).thenReturn(Optional.of(run(RunStatus.STOPPED, LocalDateTime.now())));

        assertThatThrownBy(() -> service.execute(1L, 10L, 1001L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INSPECTION_INVALID_STATE.getDefaultMessage());
    }

    private InspectionRun run(RunStatus status, LocalDateTime completedAt) {
        return run(10L, status).toBuilder().completedAt(completedAt).build();
    }

    private InspectionRun run(Long organizationId, RunStatus status) {
        return InspectionRun.builder()
                .inspectionId(1001L)
                .organizationId(organizationId)
                .userId(1L)
                .runType(RunType.REALTIME)
                .inputType("CAMERA")
                .sourceType("CAMERA")
                .sourceId("3")
                .runStatus(status)
                .appliedThreshold(BigDecimal.valueOf(0.75))
                .idempotencyKey("key")
                .startedAt(LocalDateTime.now())
                .build();
    }
}
