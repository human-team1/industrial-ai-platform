package com.example.factoryguard.config.client;

import com.example.factoryguard.adapter.out.fastapi.client.AiResponseErrorHandler;
import com.example.factoryguard.adapter.out.fastapi.client.RequestIdPropagationInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RequestIdPropagationInterceptor requestIdPropagationInterceptor() {
        return new RequestIdPropagationInterceptor();
    }

    @Bean
    public AiResponseErrorHandler aiResponseErrorHandler() {
        return new AiResponseErrorHandler();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     AiServerProperties properties,
                                     RequestIdPropagationInterceptor requestIdInterceptor,
                                     AiResponseErrorHandler aiErrorHandler) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSec()))
                .setReadTimeout(Duration.ofSeconds(properties.getReadTimeoutSec()))
                .additionalInterceptors(requestIdInterceptor)
                .errorHandler(aiErrorHandler)
                .build();
    }
}
