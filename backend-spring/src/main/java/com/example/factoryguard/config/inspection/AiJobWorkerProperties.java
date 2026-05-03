package com.example.factoryguard.config.inspection;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inspection.ai-job-worker")
public class AiJobWorkerProperties {

    private boolean enabled = true;
    private int concurrency = 1;
    private long pollIntervalMs = 1000L;
    private int batchSize = 1;
    private int maxRetry = 0;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getConcurrency() {
        return Math.max(1, concurrency);
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public long getPollIntervalMs() {
        return Math.max(200L, pollIntervalMs);
    }

    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public int getBatchSize() {
        return Math.max(1, batchSize);
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getMaxRetry() {
        return Math.max(0, maxRetry);
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }
}
