package com.example.factoryguard.application.port.in.result;

import com.example.factoryguard.application.dto.result.ResultArtifactResult;

import java.util.List;

public interface GetResultArtifactsUseCase {

    List<ResultArtifactResult> execute(Long resultId);
}
