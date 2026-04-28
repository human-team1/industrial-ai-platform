package com.example.factoryguard.domain.review.model;

import com.example.factoryguard.domain.result.vo.DecisionCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewHistory {

    private final Long reviewHistoryId;
    private final Long resultId;
    private final DecisionCode beforeDecision;
    private final DecisionCode afterDecision;
    private final String reviewComment;
    private final LocalDateTime reviewedAt;
    private final Long reviewedBy;
}
