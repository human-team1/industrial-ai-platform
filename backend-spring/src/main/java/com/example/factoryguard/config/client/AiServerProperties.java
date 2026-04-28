package com.example.factoryguard.config.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai-server")
public class AiServerProperties {

    private String baseUrl;
    private int connectTimeoutSec = 5;
    private int readTimeoutSec = 60;

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
}
