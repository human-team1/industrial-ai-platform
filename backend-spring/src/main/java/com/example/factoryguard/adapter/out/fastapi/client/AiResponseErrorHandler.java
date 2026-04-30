package com.example.factoryguard.adapter.out.fastapi.client;

import com.example.factoryguard.application.exception.ai.AiInvalidRequestException;
import com.example.factoryguard.application.exception.ai.AiServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class AiResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus status = HttpStatus.valueOf(response.getRawStatusCode());
        String summary = "AI server responded " + status.value() + " " + status.getReasonPhrase();
        if (status.is4xxClientError()) {
            throw new AiInvalidRequestException(status.value(), summary);
        }
        if (status.is5xxServerError()) {
            throw new AiServerException(status.value(), summary);
        }
        // 그 외 비정상 코드 (방어)
        throw new AiServerException(status.value(), summary);
    }
}
