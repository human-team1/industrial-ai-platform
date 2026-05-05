package com.example.factoryguard.adapter.out.fastapi.document;

import com.example.factoryguard.adapter.out.fastapi.client.FastApiClient;
import com.example.factoryguard.application.dto.document.DocumentIndexRequest;
import com.example.factoryguard.application.dto.document.DocumentIndexResponse;
import com.example.factoryguard.application.port.out.document.CallDocumentIndexingPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class DocumentIndexingFastApiAdapter implements CallDocumentIndexingPort {

    private static final String INTERNAL_INDEX_PATH = "/ai/v1/internal/documents/index";

    private final FastApiClient fastApiClient;
    private final RestTemplate restTemplate;

    @Override
    public DocumentIndexResponse index(DocumentIndexRequest request, String requestId) {
        String url = fastApiClient.baseUrl() + INTERNAL_INDEX_PATH;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (requestId != null && !requestId.isBlank()) {
            headers.set("X-Request-Id", requestId);
        }

        try {
            FastApiDocumentIndexApiResponse response = restTemplate.postForObject(
                    url,
                    new HttpEntity<>(request, headers),
                    FastApiDocumentIndexApiResponse.class
            );
            if (response == null || response.getData() == null) {
                throw new BusinessException(ErrorCode.AI_SERVER_ERROR, "AI 서버 문서 인덱싱 응답이 비어 있습니다.");
            }
            return response.getData();
        } catch (RestClientResponseException exception) {
            String detail = exception.getResponseBodyAsString();
            if (detail == null || detail.isBlank()) {
                detail = "AI 서버 문서 인덱싱 호출이 실패했습니다.";
            }
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR, summarize(detail));
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR, "AI 서버 문서 인덱싱 호출 중 통신 오류가 발생했습니다.");
        }
    }

    private String summarize(String value) {
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 500) {
            return normalized;
        }
        return normalized.substring(0, 500);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class FastApiDocumentIndexApiResponse {
        private boolean success;
        private DocumentIndexResponse data;
        private String message;
    }
}
