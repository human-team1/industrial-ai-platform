package com.example.factoryguard.adapter.out.fastapi.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AiInspectionRequest {

    private final String fileUrl;
    private final double anomalyThreshold;
    private final double lowConfidenceThreshold;
}
