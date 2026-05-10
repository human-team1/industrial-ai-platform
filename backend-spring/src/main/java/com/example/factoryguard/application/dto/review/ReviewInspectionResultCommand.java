package com.example.factoryguard.application.dto.review;

import com.example.factoryguard.domain.result.vo.DecisionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReviewInspectionResultCommand {

    private final Long adminUserId;
    private final Long resultId;
    private final DecisionCode afterDecision;
    private final String reviewComment;
}
