package com.example.factoryguard.config.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "factoryguard.model")
public class ModelMemoryBankProperties {

    private MemoryBank memoryBank = new MemoryBank();
    private Map<String, FixedProfile> fixedProfiles = new HashMap<>();

    @Getter
    @Setter
    public static class MemoryBank {
        private int minNormalImageCount = 100;
    }

    @Getter
    @Setter
    public static class FixedProfile {
        private String ckptFileKey;
        private String configFileKey;
        private String framework;
        private String inputSize;
    }
}
