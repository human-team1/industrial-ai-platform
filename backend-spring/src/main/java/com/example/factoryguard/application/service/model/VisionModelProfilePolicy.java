package com.example.factoryguard.application.service.model;

import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;

import java.math.BigDecimal;
import java.util.Map;

final class VisionModelProfilePolicy {

    private static final Map<String, Spec> SPECS = Map.of(
            key(ModelCategory.OBJECT, ModelProfile.PERFORMANCE), new Spec("336x336", "50-shot", 2000, new BigDecimal("7.4762"), new BigDecimal("6.6040")),
            key(ModelCategory.TEXTURE, ModelProfile.PERFORMANCE), new Spec("448x448", "50-shot", 2000, new BigDecimal("22.2103"), new BigDecimal("25.8270")),
            key(ModelCategory.OBJECT, ModelProfile.SPEED), new Spec("224x224", "full-shot", 10000, new BigDecimal("36.8688"), new BigDecimal("28.4368")),
            key(ModelCategory.TEXTURE, ModelProfile.SPEED), new Spec("256x256", "full-shot", 5000, new BigDecimal("40.0642"), new BigDecimal("36.5779"))
    );

    private VisionModelProfilePolicy() {
    }

    static Spec resolve(ModelCategory category, ModelProfile profile) {
        Spec spec = SPECS.get(key(category, profile));
        if (spec == null) {
            throw new IllegalArgumentException("Unsupported model profile: " + category + "/" + profile);
        }
        return spec;
    }

    private static String key(ModelCategory category, ModelProfile profile) {
        return category.name() + ":" + profile.name();
    }

    record Spec(
            String inputSize,
            String shotPolicy,
            int targetMemoryBankSize,
            BigDecimal imageThreshold,
            BigDecimal pixelThreshold
    ) {
    }
}
