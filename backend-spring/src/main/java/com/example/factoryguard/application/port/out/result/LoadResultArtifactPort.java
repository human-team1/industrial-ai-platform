package com.example.factoryguard.application.port.out.result;

import com.example.factoryguard.domain.result.model.ResultArtifact;

import java.util.List;

public interface LoadResultArtifactPort {

    List<ResultArtifact> findArtifactsByResultId(Long resultId);
}
