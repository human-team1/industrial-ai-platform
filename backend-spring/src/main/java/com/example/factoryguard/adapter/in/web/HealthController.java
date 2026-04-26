package com.example.factoryguard.adapter.in.web;

import com.example.factoryguard.adapter.out.cache.redis.RedisCacheAdapter;
import com.example.factoryguard.adapter.out.storage.minio.MinioStorageAdapter;
import com.example.factoryguard.adapter.out.vector.chroma.ChromaHealthAdapter;
import com.example.factoryguard.common.response.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final RedisCacheAdapter redisCacheAdapter;
    private final MinioStorageAdapter minioStorageAdapter;
    private final ChromaHealthAdapter chromaHealthAdapter;

    public HealthController(
            RedisCacheAdapter redisCacheAdapter,
            MinioStorageAdapter minioStorageAdapter,
            ChromaHealthAdapter chromaHealthAdapter
    ) {
        this.redisCacheAdapter = redisCacheAdapter;
        this.minioStorageAdapter = minioStorageAdapter;
        this.chromaHealthAdapter = chromaHealthAdapter;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of("status", "UP", "service", "backend-spring"));
    }

    @GetMapping("/health/infra")
    public ApiResponse<Map<String, String>> infraHealth() {
        return ApiResponse.success(Map.of(
                "redis", redisCacheAdapter.ping() ? "UP" : "DOWN",
                "minio", minioStorageAdapter.canAccessDocumentsBucket() ? "UP" : "DOWN",
                "chromadb", chromaHealthAdapter.ping() ? "UP" : "DOWN"
        ));
    }
}
