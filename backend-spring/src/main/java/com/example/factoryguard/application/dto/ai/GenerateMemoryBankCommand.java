package com.example.factoryguard.application.dto.ai;

import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GenerateMemoryBankCommand {

    private final String requestId;
    private final Long modelId;
    private final Long organizationId;
    private final Long targetId;
    private final String deploymentScope;
    private final ModelCategory modelCategory;
    private final ModelProfile modelProfile;
    private final String ckptFileKey;
    private final String configFileKey;
    private final List<String> normalImageFileKeys;
    private final String outputPrefix;
}
