package com.example.factoryguard.domain.review;

import com.example.factoryguard.domain.result.vo.DecisionCode;
import com.example.factoryguard.domain.review.model.LearningCandidate;
import com.example.factoryguard.domain.review.model.ReviewHistory;
import com.example.factoryguard.domain.review.model.ReviewQueue;
import com.example.factoryguard.domain.review.vo.LearningCandidateStatus;
import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewModelTest {

    @Test
    @DisplayName("No.22 ReviewQueue - RECHECK 결과를 WAITING + LOW_CONFIDENCE 사유로 등록")
    void buildsReviewQueueForLowConfidenceRecheck() {
        LocalDateTime now = LocalDateTime.now();
        ReviewQueue queue = ReviewQueue.builder()
                .resultId(3001L)
                .queueStatus(ReviewQueueStatus.WAITING)
                .queuedReason(ReviewQueuedReason.LOW_CONFIDENCE)
                .queuedAt(now)
                .build();

        assertThat(queue.getResultId()).isEqualTo(3001L);
        assertThat(queue.getQueueStatus()).isEqualTo(ReviewQueueStatus.WAITING);
        assertThat(queue.getQueuedReason()).isEqualTo(ReviewQueuedReason.LOW_CONFIDENCE);
        assertThat(queue.getQueuedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("No.22 ReviewQueue - 경계 점수 사유로도 등록 가능")
    void buildsReviewQueueForBoundaryScore() {
        ReviewQueue queue = ReviewQueue.builder()
                .resultId(3002L)
                .queueStatus(ReviewQueueStatus.WAITING)
                .queuedReason(ReviewQueuedReason.BOUNDARY_SCORE)
                .build();

        assertThat(queue.getQueuedReason()).isEqualTo(ReviewQueuedReason.BOUNDARY_SCORE);
    }

    @Test
    @DisplayName("No.23 ReviewHistory - 관리자 재판정 시 before/after/사유/시각/관리자 ID 저장")
    void buildsReviewHistoryWithBeforeAfter() {
        LocalDateTime reviewedAt = LocalDateTime.now();
        ReviewHistory history = ReviewHistory.builder()
                .resultId(3001L)
                .beforeDecision(DecisionCode.RECHECK)
                .afterDecision(DecisionCode.DEFECT)
                .reviewComment("표면 결함 확인됨")
                .reviewedAt(reviewedAt)
                .reviewedBy(99L)
                .build();

        assertThat(history.getResultId()).isEqualTo(3001L);
        assertThat(history.getBeforeDecision()).isEqualTo(DecisionCode.RECHECK);
        assertThat(history.getAfterDecision()).isEqualTo(DecisionCode.DEFECT);
        assertThat(history.getReviewComment()).isEqualTo("표면 결함 확인됨");
        assertThat(history.getReviewedBy()).isEqualTo(99L);
        assertThat(history.getReviewedAt()).isEqualTo(reviewedAt);
    }

    @Test
    @DisplayName("No.24 LearningCandidate - 재판정 결과를 학습 후보 CANDIDATE 상태로 등록")
    void buildsLearningCandidate() {
        LocalDateTime selectedAt = LocalDateTime.now();
        LearningCandidate candidate = LearningCandidate.builder()
                .resultId(3001L)
                .candidateStatus(LearningCandidateStatus.CANDIDATE)
                .selectedAt(selectedAt)
                .build();

        assertThat(candidate.getResultId()).isEqualTo(3001L);
        assertThat(candidate.getCandidateStatus()).isEqualTo(LearningCandidateStatus.CANDIDATE);
        assertThat(candidate.getSelectedAt()).isEqualTo(selectedAt);
    }

    @Test
    @DisplayName("No.24 LearningCandidate - SELECTED 상태로 전이 가능 (관리자 선택)")
    void learningCandidateCanTransitionToSelected() {
        LearningCandidate candidate = LearningCandidate.builder()
                .resultId(3001L)
                .candidateStatus(LearningCandidateStatus.SELECTED)
                .build();

        assertThat(candidate.getCandidateStatus()).isEqualTo(LearningCandidateStatus.SELECTED);
    }
}
