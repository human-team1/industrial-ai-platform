package com.example.factoryguard.application.dto.model;

import com.example.factoryguard.domain.model.vo.DeploymentScope;
import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class GenerateModelVersionFromNormalImagesCommand {

    private final Long modelId;
    private final Long organizationId;
    private final Long targetId;
    private final DeploymentScope deploymentScope;
    private final ModelCategory modelCategory;
    private final ModelProfile modelProfile;
    private final String versionName;
    private final BigDecimal thresholdDefault;
    private final String reason;
    private final List<MultipartFile> normalImages;
    private final Long actorUserId;
    private final String requestId;
}
