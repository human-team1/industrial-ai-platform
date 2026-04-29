package com.example.factoryguard.adapter.out.fastapi.knowledge;

import com.example.factoryguard.application.port.out.rag.RequestDocumentIndexingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class RequestDocumentIndexingAdapter implements RequestDocumentIndexingPort {

    private final RestTemplate restTemplate;

    @Value("${app.ai-server.base-url}")
    private String aiServerBaseUrl;

    @Override
    public void request(Long documentVersionId) {
        String url = aiServerBaseUrl + "/ai/v1/documents/index";

        IndexingRequest request = new IndexingRequest(documentVersionId);
        restTemplate.postForEntity(url, request, Void.class);
    }

    // 내부 요청 DTO
    record IndexingRequest(Long documentVersionId) {}
}
