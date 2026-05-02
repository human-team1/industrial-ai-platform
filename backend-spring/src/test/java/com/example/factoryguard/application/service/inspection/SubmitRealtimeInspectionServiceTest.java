package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.application.dto.inspection.SubmitInspectionResult;
import com.example.factoryguard.application.dto.inspection.SubmitRealtimeInspectionCommand;
import com.example.factoryguard.application.port.in.notification.CreateNotificationUseCase;
import com.example.factoryguard.application.port.out.auth.TokenStorePort;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import com.example.factoryguard.application.port.out.inspection.LoadAnalysisTargetPort;
import com.example.factoryguard.application.port.out.inspection.LoadCameraSourcePort;
import com.example.factoryguard.application.port.out.inspection.SaveInspectionResultPort;
import com.example.factoryguard.application.port.out.review.SaveReviewQueuePort;
import com.example.factoryguard.application.port.out.user.FindUserByIdPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.inspection.model.AnalysisTarget;
import com.example.factoryguard.domain.inspection.model.CameraSource;
import com.example.factoryguard.domain.inspection.model.CameraStatus;
import com.example.factoryguard.domain.inspection.model.InspectionEventType;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import com.example.factoryguard.domain.inspection.model.RunType;
import com.example.factoryguard.domain.user.model.ThresholdSource;
import com.example.factoryguard.domain.user.model.User;
import com.example.factoryguard.domain.user.model.UserRole;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitRealtimeInspectionServiceTest {

    @Mock TokenStorePort tokenStorePort;
    @Mock FindUserByIdPort findUserByIdPort;
    @Mock LoadAnalysisTargetPort loadAnalysisTargetPort;
    @Mock LoadCameraSourcePort loadCameraSourcePort;
    @Mock SaveInspectionResultPort saveInspectionResultPort;
    @Mock SaveReviewQueuePort saveReviewQueuePort;
    @Mock CallAiInspectionPort callAiInspectionPort;
    @Mock ResolveInspectionThresholdService resolveInspectionThresholdService;
    @Mock InspectionRunRecorder runRecorder;
    @Mock InspectionInputRecorder inputRecorder;
    @Mock InspectionEventLogger eventLogger;
    @Mock DecisionProperties decisionProperties;
    @Mock CreateNotificationUseCase createNotificationUseCase;

    SubmitRealtimeInspectionService service;

    @BeforeEach
    void setUp() {
        service = new SubmitRealtimeInspectionService(
                tokenStorePort,
                findUserByIdPort,
                loadAnalysisTargetPort,
                loadCameraSourcePort,
                saveInspectionResultPort,
                saveReviewQueuePort,
                callAiInspectionPort,
                resolveInspectionThresholdService,
                runRecorder,
                inputRecorder,
                eventLogger,
                decisionProperties,
                createNotificationUseCase
        );
    }

    @Test
    void startRealtimeInspectionWithCameraSucceeds() {
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(loadCameraSourcePort.findById(3L)).thenReturn(Optional.of(camera(3L, 10L)));
        when(runRecorder.create(any())).thenAnswer(invocation -> {
            InspectionRun run = invocation.getArgument(0);
            return run.toBuilder().inspectionId(1001L).build();
        });

        SubmitInspectionResult result = service.execute(new SubmitRealtimeInspectionCommand(
                1L, "session-1", null, 3L, null
        ));

        assertThat(result.getInspectionId()).isEqualTo(1001L);
        assertThat(result.getRunStatus()).isEqualTo(RunStatus.PROCESSING);

        ArgumentCaptor<InspectionRun> runCaptor = ArgumentCaptor.forClass(InspectionRun.class);
        verify(runRecorder).create(runCaptor.capture());
        assertThat(runCaptor.getValue().getRunType()).isEqualTo(RunType.REALTIME);
        assertThat(runCaptor.getValue().getRunStatus()).isEqualTo(RunStatus.PROCESSING);
        assertThat(runCaptor.getValue().getSourceId()).isEqualTo("3");

        ArgumentCaptor<InspectionInput> inputCaptor = ArgumentCaptor.forClass(InspectionInput.class);
        verify(inputRecorder).record(inputCaptor.capture());
        assertThat(inputCaptor.getValue().getInspectionId()).isEqualTo(1001L);
        assertThat(inputCaptor.getValue().getCameraId()).isEqualTo(3L);

        verify(eventLogger).log(1001L, InspectionEventType.REALTIME_STARTED, "실시간 탐지가 시작되었습니다.");
        verify(eventLogger).log(1001L, InspectionEventType.INPUT_SAVED, "camera input persisted");
    }

    @Test
    void missingCameraThrowsNotFound() {
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(loadCameraSourcePort.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new SubmitRealtimeInspectionCommand(
                1L, "session-1", null, 404L, null
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CAMERA_NOT_FOUND.getDefaultMessage());
    }

    @Test
    void otherOrganizationCameraThrowsForbidden() {
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(loadCameraSourcePort.findById(3L)).thenReturn(Optional.of(camera(3L, 99L)));

        assertThatThrownBy(() -> service.execute(new SubmitRealtimeInspectionCommand(
                1L, "session-1", null, 3L, null
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getDefaultMessage());
    }

    @Test
    void otherOrganizationTargetThrowsForbidden() {
        givenActiveUser();
        when(resolveInspectionThresholdService.resolve(1L, null)).thenReturn(defaultThreshold());
        when(loadAnalysisTargetPort.findById(7L)).thenReturn(Optional.of(AnalysisTarget.builder()
                .targetId(7L)
                .organizationId(99L)
                .targetName("other")
                .build()));

        assertThatThrownBy(() -> service.execute(new SubmitRealtimeInspectionCommand(
                1L, "session-1", 7L, 3L, null
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN.getDefaultMessage());
    }

    private void givenActiveUser() {
        when(tokenStorePort.getSessionId(1L)).thenReturn(Optional.of("session-1"));
        when(findUserByIdPort.findById(1L)).thenReturn(Optional.of(User.builder()
                .userId(1L)
                .organizationId(10L)
                .email("user@example.com")
                .name("user")
                .role(UserRole.ROLE_COMPANY_WORKER)
                .status(UserStatus.ACTIVE)
                .build()));
    }

    private ResolvedThreshold defaultThreshold() {
        return new ResolvedThreshold(0.75, 0.55, ThresholdSource.SYSTEM_DEFAULT, null, null);
    }

    private CameraSource camera(Long cameraId, Long organizationId) {
        return CameraSource.builder()
                .cameraId(cameraId)
                .organizationId(organizationId)
                .userId(1L)
                .cameraName("camera-" + cameraId)
                .streamUrl("rtsp://example.local/live")
                .status(CameraStatus.ACTIVE)
                .build();
    }
}
