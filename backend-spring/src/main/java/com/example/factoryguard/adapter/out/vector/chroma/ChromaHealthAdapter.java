package com.example.factoryguard.adapter.out.vector.chroma;

import com.example.factoryguard.config.persistence.ChromaProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ChromaHealthAdapter {

    private final RestTemplate restTemplate;
    private final ChromaProperties chromaProperties;

    public ChromaHealthAdapter(RestTemplate restTemplate, ChromaProperties chromaProperties) {
        this.restTemplate = restTemplate;
        this.chromaProperties = chromaProperties;
    }

    public boolean ping() {
        try {
            String url = "http://" + chromaProperties.getHost() + ":" + chromaProperties.getPort() + "/api/v1/heartbeat";
            restTemplate.getForEntity(url, String.class);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
