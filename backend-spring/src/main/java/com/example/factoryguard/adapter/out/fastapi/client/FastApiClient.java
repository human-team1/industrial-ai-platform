package com.example.factoryguard.adapter.out.fastapi.client;

import com.example.factoryguard.config.client.AiServerProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FastApiClient {

    private final RestTemplate restTemplate;
    private final AiServerProperties properties;

    public FastApiClient(RestTemplate restTemplate, AiServerProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public String baseUrl() {
        return properties.getBaseUrl();
    }
}
