package com.example.factoryguard.domain.inspection;

import com.example.factoryguard.domain.inspection.model.DecisionCode;
import com.example.factoryguard.domain.inspection.model.InspectionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InspectionResultTest {

    @Test
    @DisplayName("No.18 InspectionResult - 점수/판정/모델버전/임계값버전 모두 저장 가능")
    void buildsWithAllPersistenceFields() {
        LocalDateTime now = LocalDateTime.now();
        InspectionResult result = InspectionResult.builder()
                .resultId(7001L)
                .inspectionId(2001L)
                .score(new BigDecimal("0.8123"))
                .confidence(new BigDecimal("0.9100"))
                .decisionCode(DecisionCode.DEFECT)
                .finalDecisionCode(DecisionCode.DEFECT)
                .resultStatus("SUCCESS")
                .thresholdSource("USER")
                .thresholdId(500L)
                .thresholdVersion(3)
                .modelVersionId(10L)
                .createdAt(now)
                .build();

        assertThat(result.getResultId()).isEqualTo(7001L);
        assertThat(result.getScore()).isEqualByComparingTo("0.8123");
        assertThat(result.getConfidence()).isEqualByComparingTo("0.91");
        assertThat(result.getDecisionCode()).isEqualTo(DecisionCode.DEFECT);
        assertThat(result.getModelVersionId()).isEqualTo(10L);
        assertThat(result.getThresholdId()).isEqualTo(500L);
        assertThat(result.getThresholdVersion()).isEqualTo(3);
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("No.18 InspectionResult - 실패 사유 기록 시 resultStatus FAILED")
    void canPersistFailureReason() {
        InspectionResult result = InspectionResult.builder()
                .resultId(7002L)
                .inspectionId(2002L)
                .resultStatus("FAILED")
                .failureReason("AI server timeout")
                .build();

        assertThat(result.getResultStatus()).isEqualTo("FAILED");
        assertThat(result.getFailureReason()).isEqualTo("AI server timeout");
    }
}
