package com.example.factoryguard.adapter.out.fastapi.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GenerateMemoryBankRequest {

    private final String modelCategory;
    private final String modelProfile;
    private final List<String> normalImageFileKeys;
    private final String configFileKey;
    private final String ckptFileKey;
    private final String outputPrefix;
}
