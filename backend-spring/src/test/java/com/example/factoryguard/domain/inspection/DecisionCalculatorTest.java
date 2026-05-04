package com.example.factoryguard.domain.inspection;

import com.example.factoryguard.domain.inspection.model.DecisionCalculator;
import com.example.factoryguard.domain.inspection.model.DecisionCode;
import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionCalculatorTest {

    @Test
    @DisplayName("No.22 RECHECK 분기 - 신뢰도 부족 시 LOW_CONFIDENCE 사유로 큐 등록")
    void lowConfidenceProducesRecheckWithLowConfidenceReason() {
        DecisionCalculator.DecisionResult result = DecisionCalculator.decide(
                0.4, 0.30, 0.75, 0.55, 0.05);

        assertThat(result.decisionCode()).isEqualTo(DecisionCode.RECHECK);
        assertThat(result.queuedReason()).isEqualTo(ReviewQueuedReason.LOW_CONFIDENCE);
    }

    @Test
    @DisplayName("No.22 RECHECK 분기 - 경계 점수 시 BOUNDARY_SCORE 사유로 큐 등록")
    void boundaryScoreProducesRecheckWithBoundaryReason() {
        DecisionCalculator.DecisionResult result = DecisionCalculator.decide(
                0.78, 0.90, 0.75, 0.55, 0.05);

        assertThat(result.decisionCode()).isEqualTo(DecisionCode.RECHECK);
        assertThat(result.queuedReason()).isEqualTo(ReviewQueuedReason.BOUNDARY_SCORE);
    }

    @Test
    @DisplayName("DEFECT 분기 - 점수 임계값 초과 시 큐 등록 사유 없음")
    void scoreAboveThresholdProducesDefect() {
        DecisionCalculator.DecisionResult result = DecisionCalculator.decide(
                0.95, 0.90, 0.75, 0.55, 0.05);

        assertThat(result.decisionCode()).isEqualTo(DecisionCode.DEFECT);
        assertThat(result.queuedReason()).isNull();
    }

    @Test
    @DisplayName("NORMAL 분기 - 점수 임계값 미만 시 큐 등록 사유 없음")
    void scoreBelowThresholdProducesNormal() {
        DecisionCalculator.DecisionResult result = DecisionCalculator.decide(
                0.30, 0.90, 0.75, 0.55, 0.05);

        assertThat(result.decisionCode()).isEqualTo(DecisionCode.NORMAL);
        assertThat(result.queuedReason()).isNull();
    }
}
