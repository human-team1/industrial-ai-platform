package com.example.factoryguard.application.dto.result;

import com.example.factoryguard.domain.result.vo.AnomalyRegionLabel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AnomalyRegionResult {

    private final Long regionId;
    private final Long imageId;
    private final AnomalyRegionLabel labelCode;
    private final BigDecimal bboxX;
    private final BigDecimal bboxY;
    private final BigDecimal bboxW;
    private final BigDecimal bboxH;
    private final BigDecimal score;
}
