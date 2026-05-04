package com.example.factoryguard.application.dto.inspection;

import com.example.factoryguard.domain.inspection.model.InspectionRun;
import com.example.factoryguard.domain.inspection.model.RunStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubmitInspectionResultTest {

    @Test
    @DisplayName("No.11 업로드 검사 요청 결과는 RunStatus.PROCESSING 으로 응답된다")
    void acceptedRunReportsProcessingStatus() {
        InspectionRun run = InspectionRun.builder()
                .inspectionId(101L)
                .runStatus(RunStatus.PROCESSING)
                .build();

        SubmitInspectionResult result = SubmitInspectionResult.accepted(run, false);

        assertThat(result.getInspectionId()).isEqualTo(101L);
        assertThat(result.getRunStatus()).isEqualTo(RunStatus.PROCESSING);
        assertThat(result.isReplay()).isFalse();
    }

    @Test
    @DisplayName("No.11 PENDING → toBuilder 갱신 시 PROCESSING 상태로 노출 가능")
    void runStatusCanBeUpgradedToProcessing() {
        InspectionRun pending = InspectionRun.builder()
                .inspectionId(101L)
                .runStatus(RunStatus.PENDING)
                .build();

        InspectionRun upgraded = pending.toBuilder().runStatus(RunStatus.PROCESSING).build();

        assertThat(upgraded.getRunStatus()).isEqualTo(RunStatus.PROCESSING);
    }
}
