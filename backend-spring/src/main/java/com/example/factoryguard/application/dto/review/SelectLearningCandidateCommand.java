package com.example.factoryguard.application.dto.review;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SelectLearningCandidateCommand {

    private final Long adminUserId;
    private final Long resultId;
}
