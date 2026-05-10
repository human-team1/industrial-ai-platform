package com.example.factoryguard.application.dto.review;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RejectLearningCandidateCommand {

    private final Long adminUserId;
    private final Long learningCandidateId;
    private final String reason;
}
