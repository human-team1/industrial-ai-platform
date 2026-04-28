package com.example.factoryguard.application.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopDefectTypeResult {

    private final String defectType;
    private final Long count;
}
