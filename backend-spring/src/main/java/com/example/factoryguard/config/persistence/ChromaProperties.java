package com.example.factoryguard.config.persistence;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.chroma")
public class ChromaProperties {

    private String host;
    private int port;
    private String collectionDocuments;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCollectionDocuments() {
        return collectionDocuments;
    }

    public void setCollectionDocuments(String collectionDocuments) {
        this.collectionDocuments = collectionDocuments;
    }
}
