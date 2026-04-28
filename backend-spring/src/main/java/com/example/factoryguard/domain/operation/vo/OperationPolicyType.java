package com.example.factoryguard.domain.operation.vo;

public enum OperationPolicyType {
    THRESHOLD_LIMIT,
    SESSION_TIMEOUT,
    FILE_SIZE_LIMIT,
    DUPLICATE_LOGIN,
    MODEL_DEPLOYMENT,
    LOG_RETENTION
}
