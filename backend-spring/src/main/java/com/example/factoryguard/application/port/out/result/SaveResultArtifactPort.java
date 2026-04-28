package com.example.factoryguard.application.port.out.result;

import com.example.factoryguard.domain.result.model.ResultArtifact;

public interface SaveResultArtifactPort {

    ResultArtifact save(ResultArtifact artifact);
}
