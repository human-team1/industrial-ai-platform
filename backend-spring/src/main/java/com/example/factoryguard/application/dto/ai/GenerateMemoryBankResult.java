package com.example.factoryguard.application.dto.ai;

import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class GenerateMemoryBankResult {

    private final String memoryBankFileKey;
    private final String configFileKey;
    private final String ckptFileKey;
    private final int normalImageCount;
    private final ModelCategory modelCategory;
    private final ModelProfile modelProfile;
    private final String inputSize;
    private final String framework;
    private final LocalDateTime createdAt;
    /** AI 서버가 정상 이미지 score 분포로 보정한 threshold. null이면 policy 값을 사용한다. */
    private final BigDecimal calibratedThreshold;
}
