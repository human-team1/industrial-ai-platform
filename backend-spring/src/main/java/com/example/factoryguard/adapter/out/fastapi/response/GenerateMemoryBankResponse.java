package com.example.factoryguard.adapter.out.fastapi.response;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class GenerateMemoryBankResponse {

    private boolean success;
    private MemoryBankData data;
    private String message;

    @Getter
    public static class MemoryBankData {
        private String memoryBankFileKey;
        private String configFileKey;
        private String ckptFileKey;
        private int normalImageCount;
        private String modelCategory;
        private String modelProfile;
        private String inputSize;
        private String framework;
        private LocalDateTime createdAt;
        private BigDecimal calibratedThreshold;
    }
}
