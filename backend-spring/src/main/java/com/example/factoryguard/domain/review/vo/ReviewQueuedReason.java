package com.example.factoryguard.domain.review.vo;

public enum ReviewQueuedReason {
    LOW_CONFIDENCE,
    BOUNDARY_SCORE,
    QUALITY_FAILED,
    MODEL_FAILURE,
    MANUAL_REQUEST
}
