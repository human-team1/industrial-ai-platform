package com.example.factoryguard.adapter.out.fastapi.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GenerateMemoryBankResponse {

    private boolean success;
    private MemoryBankData data;
    private String message;

    @Getter
    public static class MemoryBankData {
        private String memoryBankFileKey;
        private int normalImageCount;
        private String modelCategory;
        private String modelProfile;
        private LocalDateTime createdAt;
    }
}
