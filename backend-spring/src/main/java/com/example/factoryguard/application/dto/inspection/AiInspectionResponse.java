package com.example.factoryguard.application.dto.inspection;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiInspectionResponse {

    private double score;
    private double confidence;
    private Long modelVersionId;
}