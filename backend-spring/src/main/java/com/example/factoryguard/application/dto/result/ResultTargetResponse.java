package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultTargetResponse {

    private final Long targetId;
    private final String targetName;
    private final String equipmentName;
    private final String productName;
}
