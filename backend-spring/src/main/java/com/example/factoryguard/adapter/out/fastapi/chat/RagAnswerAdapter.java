package com.example.factoryguard.adapter.out.fastapi.chat;

import com.example.factoryguard.adapter.out.fastapi.client.FastApiClient;
import com.example.factoryguard.application.dto.chat.RagAnswerRequest;
import com.example.factoryguard.application.dto.chat.RagAnswerResponse;
import com.example.factoryguard.application.dto.chat.RagAnswerSource;
import com.example.factoryguard.application.dto.chat.RagResultContext;
import com.example.factoryguard.application.port.out.chat.RequestRagAnswerPort;
import com.example.factoryguard.config.client.AiServerProperties;
import com.example.factoryguard.domain.chat.vo.ChatAnswerStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RagAnswerAdapter implements RequestRagAnswerPort {

    private static final Logger log = LoggerFactory.getLogger(RagAnswerAdapter.class);
    private static final String INTERNAL_RAG_PATH = "/ai/v1/internal/rag/query";
    private static final String FALLBACK_ANSWER = "RAG answer generation failed.";

    private final FastApiClient fastApiClient;
    private final RestTemplate restTemplate;
    private final AiServerProperties aiServerProperties;

    @Override
    public RagAnswerResponse requestAnswer(RagAnswerRequest request) {
        FastApiRagRequest payload = FastApiRagRequest.from(request, defaultTopK());
        try {
            log.info("FastAPI RAG request, requestId={}, endpoint={}, conversationId={}, organizationId={}, resultId={}, topK={}, hasResultContext={}, questionPreview={}",
                    MDC.get("requestId"), INTERNAL_RAG_PATH, request.getConversationId(), request.getOrganizationId(), request.getResultId(),
                    payload.getTopK(), request.getResultContext() != null, previewQuestion(request.getQuestion()));

            FastApiRagResponse response = restTemplate.postForObject(
                    fastApiClient.baseUrl() + INTERNAL_RAG_PATH,
                    payload,
                    FastApiRagResponse.class);
            RagAnswerResponse mapped = mapResponse(response == null ? null : response.getData());
            log.info("FastAPI RAG completed, requestId={}, endpoint={}, conversationId={}, organizationId={}, status={}, sourceCount={}",
                    MDC.get("requestId"), INTERNAL_RAG_PATH, request.getConversationId(), request.getOrganizationId(),
                    mapped.getAnswerStatus(), mapped.getSources() == null ? 0 : mapped.getSources().size());
            return mapped;
        } catch (ResourceAccessException e) {
            log.error("FastAPI RAG timeout, requestId={}, endpoint={}, conversationId={}, organizationId={}, message={}",
                    MDC.get("requestId"), INTERNAL_RAG_PATH, request.getConversationId(), request.getOrganizationId(), e.getMessage(), e);
            return failedResponse(ChatAnswerStatus.VECTOR_STORE_FAILED, "RAG_TIMEOUT");
        } catch (RestClientResponseException e) {
            log.error("FastAPI RAG HTTP error, requestId={}, endpoint={}, conversationId={}, organizationId={}, statusCode={}, responseBody={}",
                    MDC.get("requestId"), INTERNAL_RAG_PATH, request.getConversationId(), request.getOrganizationId(),
                    e.getRawStatusCode(), trimForLog(e.getResponseBodyAsString()), e);
            return failedResponse(ChatAnswerStatus.LLM_FAILED, "RAG_SERVER_ERROR");
        } catch (RestClientException e) {
            log.error("FastAPI RAG client error, requestId={}, endpoint={}, conversationId={}, organizationId={}, message={}",
                    MDC.get("requestId"), INTERNAL_RAG_PATH, request.getConversationId(), request.getOrganizationId(), e.getMessage(), e);
            return failedResponse(ChatAnswerStatus.LLM_FAILED, "RAG_SERVER_ERROR");
        }
    }

    private int defaultTopK() {
        return Math.max(aiServerProperties.getRag().getDefaultTopK(), 1);
    }

    private String previewQuestion(String question) {
        if (question == null || question.isBlank()) {
            return "";
        }
        String normalized = question.trim().replaceAll("\s+", " ");
        return normalized.length() <= 80 ? normalized : normalized.substring(0, 80) + "...";
    }

    private String trimForLog(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalized = text.trim().replaceAll("\s+", " ");
        return normalized.length() <= 1000 ? normalized : normalized.substring(0, 1000) + "...";
    }

    private RagAnswerResponse mapResponse(RagData data) {
        if (data == null) {
            return failedResponse(ChatAnswerStatus.LLM_FAILED, "RAG_EMPTY_RESPONSE");
        }
        ChatAnswerStatus answerStatus = parseStatus(data.getAnswerStatus());
        List<RagAnswerSource> sources = data.getSources() == null ? List.of() : data.getSources().stream()
                .map(this::toSource)
                .toList();
        return RagAnswerResponse.builder()
                .answerText(data.getAnswer())
                .answerStatus(answerStatus)
                .errorCode(answerStatus == ChatAnswerStatus.ANSWERED ? null : answerStatus.name())
                .modelName(data.getLlmModel())
                .sources(sources)
                .build();
    }

    private RagAnswerResponse failedResponse(ChatAnswerStatus status, String errorCode) {
        return RagAnswerResponse.builder()
                .answerText(FALLBACK_ANSWER)
                .answerStatus(status)
                .errorCode(errorCode)
                .sources(List.of())
                .build();
    }

    private ChatAnswerStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return ChatAnswerStatus.ANSWERED;
        }
        try {
            return ChatAnswerStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return ChatAnswerStatus.LLM_FAILED;
        }
    }

    private RagAnswerSource toSource(RagSource source) {
        return RagAnswerSource.builder()
                .sourceId(source.getDocumentId())
                .documentId(source.getDocumentId())
                .documentTitle(source.getDocumentTitle())
                .documentType(source.getDocumentType())
                .chunkId(null)
                .vectorRef(source.getChunkId())
                .page(source.getPageNo())
                .section(source.getSection())
                .sourceSnippet(source.getSourceSnippet())
                .score(source.getScore())
                .build();
    }

    @Getter
    @RequiredArgsConstructor
    private static class FastApiRagRequest {
        private final String question;
        private final Long userId;
        private final Long organizationId;
        private final Long conversationId;
        private final RagResultContext resultContext;
        private final Integer topK;

        static FastApiRagRequest from(RagAnswerRequest request, int defaultTopK) {
            Integer topK = request.getTopK() == null || request.getTopK() < 1 ? defaultTopK : request.getTopK();
            return new FastApiRagRequest(
                    request.getQuestion(),
                    request.getUserId(),
                    request.getOrganizationId(),
                    request.getConversationId(),
                    request.getResultContext(),
                    topK
            );
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class FastApiRagResponse {
        private boolean success;
        private RagData data;
        private String message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class RagData {
        private String answer;
        private String answerStatus;
        private String questionMode;
        private List<RagSource> sources = List.of();
        private String llmModel;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class RagSource {
        private Long documentId;
        private Long documentVersionId;
        private String documentTitle;
        private String documentType;
        private String chunkId;
        private Integer pageNo;
        private String section;
        private BigDecimal score;
        private String sourceSnippet;
    }
}
