package com.example.factoryguard.domain.review.model;

import com.example.factoryguard.domain.review.vo.ReviewQueuedReason;
import com.example.factoryguard.domain.review.vo.ReviewQueueStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewQueue {

    private final Long reviewQueueId;
    private final Long resultId;
    private final ReviewQueueStatus queueStatus;
    private final ReviewQueuedReason queuedReason;
    private final LocalDateTime queuedAt;
}
