package com.example.factoryguard.adapter.out.fastapi.client;

import com.example.factoryguard.adapter.out.fastapi.response.FastApiProblemDetails;
import com.example.factoryguard.application.exception.ai.AiInvalidRequestException;
import com.example.factoryguard.application.exception.ai.AiServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AiResponseErrorHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus status = HttpStatus.valueOf(response.getRawStatusCode());
        String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        String summary = summarize(status, body);
        if (status.is4xxClientError()) {
            throw new AiInvalidRequestException(status.value(), summary);
        }
        if (status.is5xxServerError()) {
            throw new AiServerException(status.value(), summary);
        }
        throw new AiServerException(status.value(), summary);
    }

    private String summarize(HttpStatus status, String body) {
        if (body != null && !body.isBlank()) {
            try {
                FastApiProblemDetails problem = objectMapper.readValue(body, FastApiProblemDetails.class);
                if (hasText(problem.getDetail())) {
                    return truncate(problem.getDetail());
                }
                if (hasText(problem.getTitle())) {
                    return truncate(problem.getTitle());
                }
            } catch (IOException ignored) {
                return truncate(body);
            }
        }
        return "AI server responded " + status.value() + " " + status.getReasonPhrase();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String truncate(String value) {
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500);
    }
}
