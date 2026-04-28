package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.domain.document.model.VectorIndex;

public interface SaveVectorIndexPort {

    VectorIndex save(VectorIndex vectorIndex);
}
