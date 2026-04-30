package com.example.factoryguard.domain.result.model;

import com.example.factoryguard.domain.result.vo.AnomalyRegionLabel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnomalyRegion {

    private final Long regionId;
    private final Long imageId;
    private final AnomalyRegionLabel labelCode;
    private final Double bboxX;
    private final Double bboxY;
    private final Double bboxW;
    private final Double bboxH;
    private final Double score;
    private final LocalDateTime createdAt;
}
