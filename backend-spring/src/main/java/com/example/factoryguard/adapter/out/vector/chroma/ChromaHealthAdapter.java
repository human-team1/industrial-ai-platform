package com.example.factoryguard.adapter.out.vector.chroma;

import com.example.factoryguard.config.persistence.ChromaProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class ChromaHealthAdapter {

    private final RestTemplate restTemplate;
    private final ChromaProperties chromaProperties;

    public ChromaHealthAdapter(RestTemplateBuilder restTemplateBuilder, ChromaProperties chromaProperties) {
        this.chromaProperties = chromaProperties;
        Duration timeout = Duration.ofMillis(Math.max(500, chromaProperties.getHealthTimeoutMs()));
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }

    public boolean ping() {
        try {
            String url = "http://" + chromaProperties.getHost() + ":" + chromaProperties.getPort() + "/api/v1/heartbeat";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception exception) {
            return false;
        }
    }
}
