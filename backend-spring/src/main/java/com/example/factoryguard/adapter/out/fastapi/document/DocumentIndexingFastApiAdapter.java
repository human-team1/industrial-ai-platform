package com.example.factoryguard.adapter.out.fastapi.document;

import com.example.factoryguard.adapter.out.fastapi.client.FastApiClient;
import com.example.factoryguard.application.dto.document.DocumentDeindexRequest;
import com.example.factoryguard.application.dto.document.DocumentDeindexResponse;
import com.example.factoryguard.application.dto.document.DocumentIndexRequest;
import com.example.factoryguard.application.dto.document.DocumentIndexResponse;
import com.example.factoryguard.application.exception.ai.AiServerException;
import com.example.factoryguard.application.port.out.document.CallDocumentIndexingPort;
import com.example.factoryguard.application.port.out.document.DocumentIndexingAiPort;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentIndexingFastApiAdapter implements DocumentIndexingAiPort, CallDocumentIndexingPort {

    private static final Logger log = LoggerFactory.getLogger(DocumentIndexingFastApiAdapter.class);
    private static final String INTERNAL_INDEX_PATH = "/ai/v1/internal/documents/index";
    private static final String INTERNAL_JOB_STATUS_PATH = "/ai/v1/internal/document-index-jobs/{aiJobId}";
    private static final String INTERNAL_DEINDEX_PATH = "/ai/v1/internal/document-versions/{documentVersionId}/index";

    private final FastApiClient fastApiClient;
    private final RestTemplate restTemplate;

    @Override
    public DocumentIndexResponse index(DocumentIndexRequest request, String requestId) {
        return enqueue(request, requestId);
    }

    @Override
    public DocumentIndexResponse enqueue(DocumentIndexRequest request, String requestId) {
        String endpoint = INTERNAL_INDEX_PATH;
        try {
            FastApiDocumentIndexResponse response = restTemplate.postForObject(
                    fastApiClient.baseUrl() + endpoint,
                    new HttpEntity<>(FastApiDocumentIndexRequest.from(request), jsonHeaders(requestId)),
                    FastApiDocumentIndexResponse.class
            );
            DocumentIndexResponse data = requireData(response == null ? null : response.getData(), endpoint);
            log.info("FastAPI document index enqueued, requestId={}, endpoint={}, indexJobId={}, documentVersionId={}, organizationId={}, status={}",
                    requestId(), endpoint, data.getIndexJobId(), data.getDocumentVersionId(), data.getOrganizationId(), data.getIndexingStatus());
            return data;
        } catch (ResourceAccessException exception) {
            throw new AiServerException("AI document indexing request timed out or connection failed", exception);
        } catch (RestClientException exception) {
            throw new AiServerException("AI document indexing request failed", exception);
        }
    }

    @Override
    public DocumentIndexResponse getStatus(String aiJobId) {
        String endpoint = INTERNAL_JOB_STATUS_PATH;
        try {
            FastApiDocumentIndexResponse response = restTemplate.getForObject(
                    fastApiClient.baseUrl() + endpoint,
                    FastApiDocumentIndexResponse.class,
                    aiJobId
            );
            DocumentIndexResponse data = requireData(response == null ? null : response.getData(), endpoint);
            log.info("FastAPI document index status fetched, requestId={}, endpoint={}, aiJobId={}, documentVersionId={}, organizationId={}, status={}",
                    requestId(), endpoint, aiJobId, data.getDocumentVersionId(), data.getOrganizationId(), data.getIndexingStatus());
            return data;
        } catch (ResourceAccessException exception) {
            throw new AiServerException("AI document index status request timed out or connection failed", exception);
        } catch (RestClientException exception) {
            throw new AiServerException("AI document index status request failed", exception);
        }
    }

    @Override
    public DocumentDeindexResponse deindex(Long documentVersionId, DocumentDeindexRequest request, String requestId) {
        String endpoint = INTERNAL_DEINDEX_PATH;
        try {
            FastApiDocumentDeindexResponse response = restTemplate.exchange(
                    fastApiClient.baseUrl() + endpoint,
                    HttpMethod.DELETE,
                    new HttpEntity<>(request, jsonHeaders(requestId)),
                    FastApiDocumentDeindexResponse.class,
                    documentVersionId
            ).getBody();
            DocumentDeindexResponse data = requireData(response == null ? null : response.getData(), endpoint);
            log.info("FastAPI document deindex completed, requestId={}, endpoint={}, documentId={}, documentVersionId={}, organizationId={}, deletedVectorCount={}",
                    requestId(), endpoint, data.getDocumentId(), data.getDocumentVersionId(), data.getOrganizationId(), data.getDeletedVectorCount());
            return data;
        } catch (ResourceAccessException exception) {
            throw new AiServerException("AI document deindex request timed out or connection failed", exception);
        } catch (RestClientException exception) {
            throw new AiServerException("AI document deindex request failed", exception);
        }
    }

    private HttpHeaders jsonHeaders(String requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (requestId != null && !requestId.isBlank()) {
            headers.set("X-Request-Id", requestId);
        }
        return headers;
    }

    private String requestId() {
        return MDC.get("requestId");
    }

    private <T> T requireData(T data, String endpoint) {
        if (data == null) {
            throw new AiServerException("AI server returned empty data from " + endpoint, null);
        }
        return data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class FastApiDocumentIndexResponse {
        private boolean success;
        private DocumentIndexResponse data;
        private String message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class FastApiDocumentDeindexResponse {
        private boolean success;
        private DocumentDeindexResponse data;
        private String message;
    }

    @Getter
    @Builder
    private static class FastApiDocumentIndexRequest {
        private Long indexJobId;
        private Long documentId;
        private Long documentVersionId;
        private Long organizationId;
        private FilePayload file;
        private MetadataPayload metadata;
        private ChunkingPayload chunking;
        private EmbeddingPayload embedding;

        static FastApiDocumentIndexRequest from(DocumentIndexRequest request) {
            return FastApiDocumentIndexRequest.builder()
                    .indexJobId(request.getIndexJobId())
                    .documentId(request.getDocumentId())
                    .documentVersionId(request.getDocumentVersionId())
                    .organizationId(request.getOrganizationId())
                    .file(FilePayload.builder()
                            .fileId(request.getFileId())
                            .fileKey(request.getFileKey())
                            .fileName(request.getFileName())
                            .mimeType(request.getMimeType())
                            .checksum(request.getChecksum())
                            .build())
                    .metadata(MetadataPayload.builder()
                            .title(request.getTitle())
                            .documentType(request.getDocumentType())
                            .category(request.getCategory())
                            .equipmentType(request.getEquipmentType())
                            .tags(request.getTags() == null ? List.of() : request.getTags())
                            .build())
                    .chunking(ChunkingPayload.builder()
                            .chunkSize(request.getChunkSize())
                            .chunkOverlap(request.getChunkOverlap())
                            .build())
                    .embedding(EmbeddingPayload.builder()
                            .embeddingModel(request.getEmbeddingModel())
                            .build())
                    .build();
        }
    }

    @Getter
    @Builder
    private static class FilePayload {
        private Long fileId;
        private String fileKey;
        private String fileName;
        private String mimeType;
        private String checksum;
    }

    @Getter
    @Builder
    private static class MetadataPayload {
        private String title;
        private String documentType;
        private String category;
        private String equipmentType;
        private List<String> tags;
    }

    @Getter
    @Builder
    private static class ChunkingPayload {
        private Integer chunkSize;
        private Integer chunkOverlap;
    }

    @Getter
    @Builder
    private static class EmbeddingPayload {
        private String embeddingModel;
    }
}
