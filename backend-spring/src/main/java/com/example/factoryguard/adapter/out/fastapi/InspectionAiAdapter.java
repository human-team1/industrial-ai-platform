package com.example.factoryguard.adapter.out.fastapi;

import com.example.factoryguard.adapter.out.fastapi.client.FastApiClient;
import com.example.factoryguard.adapter.out.fastapi.request.AiInspectionRequest;
import com.example.factoryguard.adapter.out.fastapi.response.AiInspectionResponse;
import com.example.factoryguard.application.dto.inspection.AiInspectionCommand;
import com.example.factoryguard.application.dto.inspection.AiInspectionResult;
import com.example.factoryguard.application.dto.inspection.AiRealtimeInspectionCommand;
import com.example.factoryguard.application.exception.ai.AiServerException;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class InspectionAiAdapter implements CallAiInspectionPort {

    private final FastApiClient fastApiClient;
    private final RestTemplate restTemplate;

    @Override
    public AiInspectionResult call(AiInspectionCommand command) throws TimeoutException {
        String url = fastApiClient.baseUrl() + "/inspect";
        AiInspectionRequest request = toRequest(command);
        try {
            AiInspectionResponse response = restTemplate.postForObject(url, request, AiInspectionResponse.class);
            return toResult(response);
        } catch (ResourceAccessException e) {
            if (hasCause(e, SocketTimeoutException.class)) {
                throw (TimeoutException) new TimeoutException(e.getMessage()).initCause(e);
            }
            throw new AiServerException("AI server connection failed: " + e.getMessage(), e);
        }
    }

    @Override
    public AiInspectionResult callRealtime(AiRealtimeInspectionCommand command) {
        throw new UnsupportedOperationException("realtime inspection not implemented yet");
    }

    private AiInspectionRequest toRequest(AiInspectionCommand command) {
        return new AiInspectionRequest(
                command.getFileUrl(),
                command.getAnomalyThreshold(),
                command.getLowConfidenceThreshold()
        );
    }

    private AiInspectionResult toResult(AiInspectionResponse response) {
        if (response == null) {
            return null;
        }
        return new AiInspectionResult(
                response.getScore(),
                response.getConfidence(),
                response.getModelVersionId()
        );
    }

    private static boolean hasCause(Throwable throwable, Class<?> targetType) {
        Throwable current = throwable;
        while (current != null) {
            if (targetType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
