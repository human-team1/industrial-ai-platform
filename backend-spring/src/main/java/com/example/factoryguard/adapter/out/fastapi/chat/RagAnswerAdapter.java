package com.example.factoryguard.adapter.out.fastapi.chat;

import com.example.factoryguard.adapter.out.fastapi.client.FastApiClient;
import com.example.factoryguard.application.dto.chat.RagAnswerRequest;
import com.example.factoryguard.application.dto.chat.RagAnswerResponse;
import com.example.factoryguard.application.dto.chat.RagAnswerSource;
import com.example.factoryguard.application.port.out.chat.RequestRagAnswerPort;
import com.example.factoryguard.domain.chat.vo.ChatAnswerStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RagAnswerAdapter implements RequestRagAnswerPort {

    private static final String NO_SOURCE_ANSWER =
            "참조 가능한 문서를 찾지 못했습니다. 설비명, 증상, 문서명을 더 구체적으로 입력해 주세요.";

    private final FastApiClient fastApiClient;
    private final RestTemplate restTemplate;

    @Override
    public RagAnswerResponse requestAnswer(RagAnswerRequest request) {
        try {
            RagQueryResponse response = restTemplate.postForObject(
                    fastApiClient.baseUrl() + "/ai/v1/rag/query",
                    new RagQueryRequest(request.getQuestion(), 5),
                    RagQueryResponse.class);
            return mapResponse(response);
        } catch (ResourceAccessException e) {
            return failedResponse(ChatAnswerStatus.VECTOR_STORE_FAILED, "RAG_TIMEOUT");
        } catch (RestClientException e) {
            return failedResponse(ChatAnswerStatus.LLM_FAILED, "RAG_SERVER_ERROR");
        }
    }

    private RagAnswerResponse mapResponse(RagQueryResponse response) {
        if (response == null) {
            return failedResponse(ChatAnswerStatus.LLM_FAILED, "RAG_EMPTY_RESPONSE");
        }
        ChatAnswerStatus answerStatus = parseStatus(response.getAnswerStatus());
        List<RagAnswerSource> sources = response.getSources() == null ? List.of() : response.getSources().stream()
                .map(this::toSource)
                .filter(source -> source.getDocumentId() != null || source.getChunkId() != null || source.getSourceSnippet() != null)
                .toList();

        if (answerStatus != ChatAnswerStatus.ANSWERED) {
            return RagAnswerResponse.builder()
                    .answerText(hasText(response.getAnswer()) ? response.getAnswer() : NO_SOURCE_ANSWER)
                    .answerStatus(answerStatus)
                    .errorCode(response.getErrorCode())
                    .modelName(response.getModelName())
                    .sources(sources)
                    .build();
        }
        if (sources.isEmpty()) {
            return RagAnswerResponse.builder()
                    .answerText(NO_SOURCE_ANSWER)
                    .answerStatus(ChatAnswerStatus.NO_RELEVANT_SOURCE)
                    .modelName(response.getModelName())
                    .sources(List.of())
                    .build();
        }
        return RagAnswerResponse.builder()
                .answerText(response.getAnswer())
                .answerStatus(ChatAnswerStatus.ANSWERED)
                .modelName(response.getModelName())
                .sources(sources)
                .build();
    }

    private RagAnswerResponse failedResponse(ChatAnswerStatus status, String errorCode) {
        return RagAnswerResponse.builder()
                .answerText("챗봇 답변을 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.")
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private RagAnswerSource toSource(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return RagAnswerSource.builder().build();
        }
        return RagAnswerSource.builder()
                .sourceId(toLong(raw.get("sourceId")))
                .documentId(toLong(raw.get("documentId")))
                .documentTitle(toString(raw.get("documentTitle")))
                .documentType(toString(raw.get("documentType")))
                .chunkId(toLong(raw.get("chunkId")))
                .page(toInteger(raw.get("page")))
                .section(toString(raw.get("section")))
                .sourceSnippet(toString(raw.get("snippet")))
                .score(toBigDecimal(raw.get("score")))
                .build();
    }

    private Long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private Integer toInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return value instanceof Number number ? BigDecimal.valueOf(number.doubleValue()) : null;
    }

    @Getter
    @RequiredArgsConstructor
    private static class RagQueryRequest {
        private final String question;
        private final int topK;
    }

    @Getter
    @Setter
    private static class RagQueryResponse {
        private String answer;
        private String answerStatus;
        private List<Object> sources = List.of();
        private String modelName;
        private Long latencyMs;
        private String errorCode;
    }
}
