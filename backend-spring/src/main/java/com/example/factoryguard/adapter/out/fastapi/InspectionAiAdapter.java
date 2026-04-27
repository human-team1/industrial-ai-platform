package com.example.factoryguard.adapter.out.fastapi;

import com.example.factoryguard.application.dto.inspection.AiInspectionRequest;
import com.example.factoryguard.application.dto.inspection.AiInspectionResponse;
import com.example.factoryguard.application.port.out.inspection.CallAiInspectionPort;
import com.example.factoryguard.adapter.out.fastapi.client.FastApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class InspectionAiAdapter implements CallAiInspectionPort {

    private final FastApiClient fastApiClient;
    private final RestTemplate restTemplate;

    @Override
    public AiInspectionResponse call(AiInspectionRequest request) {
        String url = fastApiClient.baseUrl() + "/inspect";
        return restTemplate.postForObject(url, request, AiInspectionResponse.class);
    }
}