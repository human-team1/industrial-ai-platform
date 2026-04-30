package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnomalyRegionResponse {

    private final Long regionId;
    private final String labelCode;
    private final Double bboxX;
    private final Double bboxY;
    private final Double bboxW;
    private final Double bboxH;
    private final Double score;
}
