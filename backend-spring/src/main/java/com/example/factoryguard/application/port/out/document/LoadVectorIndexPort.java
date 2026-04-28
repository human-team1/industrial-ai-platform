package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.domain.document.model.VectorIndex;

import java.util.Optional;

public interface LoadVectorIndexPort {

    Optional<VectorIndex> findByChunkId(Long chunkId);
}
