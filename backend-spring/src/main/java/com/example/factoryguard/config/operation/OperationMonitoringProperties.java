package com.example.factoryguard.config.operation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.operation.monitoring")
public class OperationMonitoringProperties {

    private long collectIntervalMs = 30000L;
    private long redisTtlSeconds = 60L;
    private int aiStatusTimeoutSec = 3;
    private String aiStatusPath = "/ai/v1/internal/system-status";
    private String instanceId = "spring-local-1";

    public long getCollectIntervalMs() {
        return collectIntervalMs;
    }

    public void setCollectIntervalMs(long collectIntervalMs) {
        this.collectIntervalMs = collectIntervalMs;
    }

    public long getRedisTtlSeconds() {
        return redisTtlSeconds;
    }

    public void setRedisTtlSeconds(long redisTtlSeconds) {
        this.redisTtlSeconds = redisTtlSeconds;
    }

    public int getAiStatusTimeoutSec() {
        return aiStatusTimeoutSec;
    }

    public void setAiStatusTimeoutSec(int aiStatusTimeoutSec) {
        this.aiStatusTimeoutSec = aiStatusTimeoutSec;
    }

    public String getAiStatusPath() {
        return aiStatusPath;
    }

    public void setAiStatusPath(String aiStatusPath) {
        this.aiStatusPath = aiStatusPath;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
