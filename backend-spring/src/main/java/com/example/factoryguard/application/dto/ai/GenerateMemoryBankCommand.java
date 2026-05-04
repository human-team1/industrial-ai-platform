package com.example.factoryguard.application.dto.ai;

import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GenerateMemoryBankCommand {

    private final ModelCategory modelCategory;
    private final ModelProfile modelProfile;
    private final List<String> normalImageFileKeys;
    private final String configFileKey;
    private final String ckptFileKey;
    private final String outputPrefix;
}
