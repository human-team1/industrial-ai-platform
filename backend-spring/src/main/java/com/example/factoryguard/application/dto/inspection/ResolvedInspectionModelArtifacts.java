package com.example.factoryguard.application.dto.inspection;

import com.example.factoryguard.adapter.out.persistence.model.ModelDeploymentJpaEntity;
import com.example.factoryguard.adapter.out.persistence.model.ModelVersionJpaEntity;
import com.example.factoryguard.domain.file.model.StoredFile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResolvedInspectionModelArtifacts {

    private final ModelDeploymentJpaEntity deployment;
    private final ModelVersionJpaEntity version;
    private final StoredFile ckpt;
    private final StoredFile config;
    private final StoredFile memoryBank;
    private final StoredFile labels;
}
