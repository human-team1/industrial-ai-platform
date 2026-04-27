package com.example.factoryguard.adapter.in.web.inspection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubmitInspectionRequest {

    private Long targetId;
    private Long thresholdId;
}