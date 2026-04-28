package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AnomalyRegionResponse {

    private final Long regionId;
    private final String labelCode;
    private final BigDecimal bboxX;
    private final BigDecimal bboxY;
    private final BigDecimal bboxW;
    private final BigDecimal bboxH;
    private final BigDecimal score;
}
