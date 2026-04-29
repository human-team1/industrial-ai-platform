package com.example.factoryguard.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResultImageResponse {

    private final Long imageId;
    private final Long fileId;
    private final String imageRole;
    private final List<AnomalyRegionResponse> regions;
}
