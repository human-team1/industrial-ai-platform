package com.example.factoryguard.domain.chat.vo;

public enum ChatAnswerStatus {
    ANSWERED,
    NO_RELEVANT_SOURCE,
    LLM_FAILED,
    VECTOR_STORE_FAILED,
    DOCUMENT_SCOPE_FORBIDDEN,
    VALIDATION_FAILED
}
