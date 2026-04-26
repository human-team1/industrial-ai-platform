package com.example.factoryguard.config.storage;

import com.example.factoryguard.adapter.out.storage.minio.MinioProperties;
import io.minio.MinioClient;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        URI endpoint = URI.create(properties.getEndpoint());
        return MinioClient.builder()
                .endpoint(endpoint.toString())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
