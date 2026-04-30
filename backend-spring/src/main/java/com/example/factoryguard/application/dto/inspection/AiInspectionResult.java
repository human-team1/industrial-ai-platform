package com.example.factoryguard.application.dto.inspection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AiInspectionResult {

    private final double score;
    private final double confidence;
    private final Long modelVersionId;
}
