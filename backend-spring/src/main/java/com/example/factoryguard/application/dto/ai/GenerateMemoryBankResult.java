package com.example.factoryguard.application.dto.ai;

import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GenerateMemoryBankResult {

    private final String memoryBankFileKey;
    private final int normalImageCount;
    private final ModelCategory modelCategory;
    private final ModelProfile modelProfile;
    private final LocalDateTime createdAt;
}
