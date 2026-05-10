package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.port.out.inspection.LoadInspectionEventLogPort;
import com.example.factoryguard.application.port.out.inspection.LoadInspectionRunPort;
import com.example.factoryguard.common.exception.BusinessException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InspectionQueryServiceTest {

    @Mock LoadInspectionRunPort loadInspectionRunPort;
    @Mock LoadInspectionEventLogPort loadInspectionEventLogPort;

    InspectionQueryService service;

    @BeforeEach
    void setUp() {
        service = new InspectionQueryService(loadInspectionRunPort, loadInspectionEventLogPort);
    }

    @Test
    void listInspectionsReturnsOrganizationRuns() {
        InspectionRun run = run(1001L, 1L);
        when(loadInspectionRunPort.findRunsByOrganizationId(1L, 0, 20)).thenReturn(List.of(run));

        List<InspectionRun> result = service.list(1L, 0, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInspectionId()).isEqualTo(1001L);
    }

    @Test
    void detailRejectsOtherOrganizationRun() {
        when(loadInspectionRunPort.findRunById(1001L)).thenReturn(Optional.of(run(1001L, 2L)));

        assertThatThrownBy(() -> service.detail(1L, 1001L))
                .isInstanceOf(BusinessException.class);
    }

    private InspectionRun run(Long inspectionId, Long organizationId) {
        return InspectionRun.builder()
                .inspectionId(inspectionId)
                .organizationId(organizationId)
                .userId(1L)
                .runType(RunType.UPLOAD)
                .inputType("FILE")
                .sourceType("UPLOAD")
                .sourceId("sample.png")
                .runStatus(RunStatus.PROCESSING)
                .appliedThreshold(BigDecimal.valueOf(0.75))
                .idempotencyKey("key")
                .startedAt(LocalDateTime.now())
                .build();
    }
}
