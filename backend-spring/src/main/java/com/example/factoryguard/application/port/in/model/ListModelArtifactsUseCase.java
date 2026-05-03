package com.example.factoryguard.application.port.in.model;

import com.example.factoryguard.application.dto.model.ModelArtifactResponse;

import java.util.List;

public interface ListModelArtifactsUseCase {

    List<ModelArtifactResponse> listModelArtifacts(Long versionId);
}
