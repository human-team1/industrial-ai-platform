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
        FastApiProblemDetails problem = parseProblem(body);
        String summary = summarize(status, body, problem);
        if (status.is4xxClientError()) {
            throw new AiInvalidRequestException(
                    status.value(),
                    summary,
                    problem == null ? null : problem.getErrorCode(),
                    problem == null ? summary : problem.getDetail(),
                    problem == null ? null : problem.getRequestId()
            );
        }
        if (status.is5xxServerError()) {
            throw new AiServerException(
                    status.value(),
                    summary,
                    problem == null ? null : problem.getErrorCode(),
                    problem == null ? summary : problem.getDetail(),
                    problem == null ? null : problem.getRequestId()
            );
        }
        throw new AiServerException(status.value(), summary);
    }

    private FastApiProblemDetails parseProblem(String body) {
        if (body != null && !body.isBlank()) {
            try {
                return objectMapper.readValue(body, FastApiProblemDetails.class);
            } catch (IOException ignored) {
                return null;
            }
        }
        return null;
    }

    private String summarize(HttpStatus status, String body, FastApiProblemDetails problem) {
        if (problem != null) {
            if (hasText(problem.getErrorCode()) && hasText(problem.getDetail())) {
                return truncate(problem.getErrorCode() + ": " + problem.getDetail());
            }
            if (hasText(problem.getDetail())) {
                return truncate(problem.getDetail());
            }
            if (hasText(problem.getTitle())) {
                return truncate(problem.getTitle());
            }
        }
        if (body != null && !body.isBlank()) {
            return truncate(body);
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
