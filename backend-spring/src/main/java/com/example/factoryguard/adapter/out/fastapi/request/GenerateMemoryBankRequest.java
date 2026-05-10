package com.example.factoryguard.adapter.out.fastapi.request;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class GenerateMemoryBankRequest {

    private final String modelCategory;
    private final String modelProfile;
    private final String ckptFileKey;
    private final String configFileKey;
    private final List<String> normalImageFileKeys;
    private final String outputPrefix;
    private final String inputSize;
    private final Integer targetMemoryBankSize;
    private final String shotPolicy;
    private final BigDecimal imageThreshold;
    private final BigDecimal pixelThreshold;
}
