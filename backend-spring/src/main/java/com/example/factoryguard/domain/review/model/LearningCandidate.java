package com.example.factoryguard.domain.review.model;

import com.example.factoryguard.domain.review.vo.LearningCandidateStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LearningCandidate {

    private final Long learningCandidateId;
    private final Long resultId;
    private final LearningCandidateStatus candidateStatus;
    private final LocalDateTime selectedAt;
}
