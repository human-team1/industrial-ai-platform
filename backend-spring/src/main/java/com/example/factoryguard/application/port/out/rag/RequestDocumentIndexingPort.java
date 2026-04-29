package com.example.factoryguard.application.port.out.rag;

public interface RequestDocumentIndexingPort {

    void request(Long documentVersionId);
}