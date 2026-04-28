package com.example.factoryguard.application.port.out.document;

import com.example.factoryguard.domain.document.model.Chunk;

import java.util.List;

public interface LoadChunkPort {

    List<Chunk> findAllByDocumentVersionId(Long documentVersionId);
}
