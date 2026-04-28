package com.example.factoryguard.domain.inspection.model;

import com.example.factoryguard.domain.inspection.vo.AnalysisTargetStatus;
import com.example.factoryguard.domain.inspection.vo.AnalysisTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnalysisTarget {

    private final Long targetId;
    private final Long organizationId;
    private final String targetName;
    private final String equipmentName;
    private final String productName;
    private final AnalysisTargetType targetType;
    private final AnalysisTargetStatus targetStatus;
    private final Long createdBy;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
