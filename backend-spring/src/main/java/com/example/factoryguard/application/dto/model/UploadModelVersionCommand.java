package com.example.factoryguard.application.dto.model;

import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Builder
public class UploadModelVersionCommand {

    private final Long modelId;
    private final String versionName;
    private final ModelCategory modelCategory;
    private final ModelProfile modelProfile;
    private final String framework;
    private final String inputSize;
    private final BigDecimal thresholdDefault;
    private final BigDecimal accuracy;
    private final BigDecimal precisionScore;
    private final BigDecimal recallScore;
    private final BigDecimal f1Score;
    private final BigDecimal aurocScore;
    private final MultipartFile ckptFile;
    private final MultipartFile configFile;
    private final MultipartFile labelsFile;
}
