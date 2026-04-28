package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.domain.document.model.Chunk;

public interface SaveChunkPort {

    Chunk save(Chunk chunk);
}
