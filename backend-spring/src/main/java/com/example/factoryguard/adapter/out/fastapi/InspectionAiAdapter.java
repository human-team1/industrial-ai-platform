package com.example.factoryguard.adapter.out.fastapi;

import com.example.factoryguard.adapter.out.fastapi.client.AiServerException;
import com.example.factoryguard.application.dto.inspection.AiInspectionRequest;
import com.example.factoryguard.application.dto.inspection.AiInspectionResponse;
import com.example.factoryguard.application.dto.inspection.AiRealtimeInspectionRequest;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import com.example.factoryguard.adapter.out.fastapi.client.FastApiClient;
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
    public AiInspectionResponse call(AiInspectionRequest request) throws TimeoutException {
        String url = fastApiClient.baseUrl() + "/inspect";
        try {
            return restTemplate.postForObject(url, request, AiInspectionResponse.class);
        } catch (ResourceAccessException e) {
            // SocketTimeoutException이 cause chain 어디에라도 있으면 AI_TIMEOUT 매핑
            if (hasCause(e, SocketTimeoutException.class)) {
                throw (TimeoutException) new TimeoutException(e.getMessage()).initCause(e);
            }
            // 그 외 connection refused 등 통신 실패 → AI_SERVER_ERROR 매핑
            throw new AiServerException("AI server connection failed: " + e.getMessage(), e);
        }
        // 4xx/5xx는 AiResponseErrorHandler가 AiInvalidRequestException/AiServerException으로 던짐
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

    @Override
    public AiInspectionResponse callRealtime(AiRealtimeInspectionRequest request) {
        // realtime AI 어댑터는 컨트롤러 레벨에서 REALTIME_NOT_ENABLED로 차단되므로 여기 도달하지 않음.
        throw new UnsupportedOperationException("realtime inspection not implemented yet");
    }
}
