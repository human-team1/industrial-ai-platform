package com.example.factoryguard.config.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai-server")
public class AiServerProperties {

    private String baseUrl;
    private int connectTimeoutSec = 5;
    private int readTimeoutSec = 60;
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 60000;
    private DocumentIndex documentIndex = new DocumentIndex();
    private Rag rag = new Rag();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectTimeoutSec() {
        return connectTimeoutSec;
    }

    public void setConnectTimeoutSec(int connectTimeoutSec) {
        this.connectTimeoutSec = connectTimeoutSec;
    }

    public int getReadTimeoutSec() {
        return readTimeoutSec;
    }

    public void setReadTimeoutSec(int readTimeoutSec) {
        this.readTimeoutSec = readTimeoutSec;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public DocumentIndex getDocumentIndex() {
        return documentIndex;
    }

    public void setDocumentIndex(DocumentIndex documentIndex) {
        this.documentIndex = documentIndex;
    }

    public Rag getRag() {
        return rag;
    }

    public void setRag(Rag rag) {
        this.rag = rag;
    }

    public static class DocumentIndex {
        private int defaultChunkSize = 800;
        private int defaultChunkOverlap = 100;
        private String defaultEmbeddingModel = "BAAI/bge-m3";
        private boolean pollingEnabled = true;
        private long pollIntervalMs = 10000L;
        private int pollBatchSize = 20;

        public int getDefaultChunkSize() {
            return defaultChunkSize;
        }

        public void setDefaultChunkSize(int defaultChunkSize) {
            this.defaultChunkSize = defaultChunkSize;
        }

        public int getDefaultChunkOverlap() {
            return defaultChunkOverlap;
        }

        public void setDefaultChunkOverlap(int defaultChunkOverlap) {
            this.defaultChunkOverlap = defaultChunkOverlap;
        }

        public String getDefaultEmbeddingModel() {
            return defaultEmbeddingModel;
        }

        public void setDefaultEmbeddingModel(String defaultEmbeddingModel) {
            this.defaultEmbeddingModel = defaultEmbeddingModel;
        }

        public boolean isPollingEnabled() {
            return pollingEnabled;
        }

        public void setPollingEnabled(boolean pollingEnabled) {
            this.pollingEnabled = pollingEnabled;
        }

        public long getPollIntervalMs() {
            return pollIntervalMs;
        }

        public void setPollIntervalMs(long pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
        }

        public int getPollBatchSize() {
            return pollBatchSize;
        }

        public void setPollBatchSize(int pollBatchSize) {
            this.pollBatchSize = pollBatchSize;
        }
    }

    public static class Rag {
        private int defaultTopK = 5;

        public int getDefaultTopK() {
            return defaultTopK;
        }

        public void setDefaultTopK(int defaultTopK) {
            this.defaultTopK = defaultTopK;
        }
    }
}
