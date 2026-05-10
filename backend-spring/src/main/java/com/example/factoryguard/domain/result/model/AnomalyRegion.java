package com.example.factoryguard.domain.result.model;

import com.example.factoryguard.domain.result.vo.AnomalyRegionLabel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AnomalyRegion {

    private final Long regionId;
    private final Long imageId;
    private final AnomalyRegionLabel labelCode;
    private final BigDecimal bboxX;
    private final BigDecimal bboxY;
    private final BigDecimal bboxW;
    private final BigDecimal bboxH;
    private final BigDecimal score;
    private final LocalDateTime createdAt;
}
