package com.example.factoryguard.application.dto.result;

import com.example.factoryguard.domain.result.vo.AnomalyRegionLabel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnomalyRegionResult {

    private final Long regionId;
    private final Long imageId;
    private final AnomalyRegionLabel labelCode;
    private final Double bboxX;
    private final Double bboxY;
    private final Double bboxW;
    private final Double bboxH;
    private final Double score;
}
