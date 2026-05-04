package com.example.factoryguard.application.service.inspection;

import com.example.factoryguard.application.dto.inspection.AiInspectionResult;
import com.example.factoryguard.application.dto.inspection.ResolvedThreshold;
import com.example.factoryguard.domain.inspection.model.DecisionCode;
import com.example.factoryguard.domain.inspection.model.InspectionInput;
import com.example.factoryguard.domain.inspection.vo.RoiMode;
import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;
import com.example.factoryguard.domain.user.model.ThresholdSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InspectionDecisionEvaluatorTest {

    private DecisionProperties decisionProperties;
    private InspectionDecisionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        decisionProperties = new DecisionProperties();
        decisionProperties.setBoundaryMargin(0.05);
        evaluator = new InspectionDecisionEvaluator(decisionProperties);
    }

    @Test
    @DisplayName("No.15 NORMAL - 점수 < threshold, confidence 정상 -> queuedReason 없음")
    void normalProducesNoQueuedReason() {
        AiInspectionResult result = result(0.30, 0.90, "PASSED", "OVERRIDE_NORMAL");
        InspectionDecisionEvaluator.Outcome outcome = evaluator.evaluate(result, fullFrameInput(true), threshold(0.75, 0.55));

        assertThat(outcome.decisionCode()).isEqualTo(DecisionCode.NORMAL);
        assertThat(outcome.queuedReason()).isNull();
    }

    @Test
    @DisplayName("No.15 DEFECT - 점수 >= threshold, confidence 정상 -> queuedReason 없음")
    void defectProducesNoQueuedReason() {
        AiInspectionResult result = result(0.95, 0.90, "PASSED", "OVERRIDE_NORMAL");
        InspectionDecisionEvaluator.Outcome outcome = evaluator.evaluate(result, fullFrameInput(true), threshold(0.75, 0.55));

        assertThat(outcome.decisionCode()).isEqualTo(DecisionCode.DEFECT);
        assertThat(outcome.queuedReason()).isNull();
    }

    @Test
    @DisplayName("No.16 RECHECK + LOW_CONFIDENCE - confidence 부족")
    void lowConfidenceProducesRecheckLowConfidence() {
        AiInspectionResult result = result(0.30, 0.30, "PASSED", "OVERRIDE_NORMAL");
        InspectionDecisionEvaluator.Outcome outcome = evaluator.evaluate(result, fullFrameInput(true), threshold(0.75, 0.55));

        assertThat(outcome.decisionCode()).isEqualTo(DecisionCode.RECHECK);
        assertThat(outcome.queuedReason()).isEqualTo(ReviewQueuedReason.LOW_CONFIDENCE);
    }

    @Test
    @DisplayName("No.22 RECHECK + BOUNDARY_SCORE - 점수가 임계값 인접 범위")
    void boundaryScoreProducesRecheckBoundary() {
        AiInspectionResult result = result(0.78, 0.90, "PASSED", "OVERRIDE_NORMAL");
        InspectionDecisionEvaluator.Outcome outcome = evaluator.evaluate(result, fullFrameInput(true), threshold(0.75, 0.55));

        assertThat(outcome.decisionCode()).isEqualTo(DecisionCode.RECHECK);
        assertThat(outcome.queuedReason()).isEqualTo(ReviewQueuedReason.BOUNDARY_SCORE);
    }

    @Test
    @DisplayName("No.22 RECHECK + QUALITY_FAILED - quality.status=FAILED")
    void qualityFailedProducesRecheckQualityFailed() {
        AiInspectionResult result = result(0.95, 0.90, "FAILED", "OVERRIDE_NORMAL");
        InspectionDecisionEvaluator.Outcome outcome = evaluator.evaluate(result, fullFrameInput(true), threshold(0.75, 0.55));

        assertThat(outcome.decisionCode()).isEqualTo(DecisionCode.RECHECK);
        assertThat(outcome.queuedReason()).isEqualTo(ReviewQueuedReason.QUALITY_FAILED);
    }

    @Test
    @DisplayName("No.15 FastAPI decisionCode 무시 - FastAPI가 NORMAL을 보내도 Spring은 BOUNDARY_SCORE 기준 RECHECK 결정")
    void ignoresFastApiDecisionCode() {
        // FastAPI가 NORMAL이라고 응답해도 Spring 자체 계산이 우선
        AiInspectionResult result = result(0.78, 0.90, "PASSED", "NORMAL");
        InspectionDecisionEvaluator.Outcome outcome = evaluator.evaluate(result, fullFrameInput(true), threshold(0.75, 0.55));

        assertThat(outcome.decisionCode()).isEqualTo(DecisionCode.RECHECK);
        assertThat(outcome.queuedReason()).isEqualTo(ReviewQueuedReason.BOUNDARY_SCORE);
    }

    @Test
    @DisplayName("qualityGateEnabled=false면 quality.status=FAILED여도 점수 기반 판정 사용")
    void qualityGateDisabledFallsBackToScoreBasedDecision() {
        AiInspectionResult result = result(0.30, 0.90, "FAILED", "OVERRIDE_NORMAL");
        InspectionDecisionEvaluator.Outcome outcome = evaluator.evaluate(result, fullFrameInput(false), threshold(0.75, 0.55));

        assertThat(outcome.decisionCode()).isEqualTo(DecisionCode.NORMAL);
        assertThat(outcome.queuedReason()).isNull();
    }

    private AiInspectionResult result(double score, double confidence, String qualityStatus, String fastApiDecisionCode) {
        return AiInspectionResult.builder()
                .score(score)
                .confidence(confidence)
                .modelVersionId(7L)
                .decisionCode(fastApiDecisionCode)
                .quality(AiInspectionResult.Quality.builder().status(qualityStatus).build())
                .build();
    }

    private ResolvedThreshold threshold(double anomaly, double lowConfidence) {
        return new ResolvedThreshold(anomaly, lowConfidence, ThresholdSource.SYSTEM_DEFAULT, null, null);
    }

    private InspectionInput fullFrameInput(boolean qualityGateEnabled) {
        return InspectionInput.builder()
                .inspectionId(100L)
                .roiMode(RoiMode.FULL_FRAME)
                .qualityGateEnabled(qualityGateEnabled)
                .build();
    }
}
