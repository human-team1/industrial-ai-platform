package com.example.factoryguard.adapter.out.fastapi;

import com.example.factoryguard.adapter.out.fastapi.client.FastApiClient;
import com.example.factoryguard.adapter.out.fastapi.request.GenerateMemoryBankRequest;
import com.example.factoryguard.adapter.out.fastapi.response.GenerateMemoryBankResponse;
import com.example.factoryguard.application.dto.ai.GenerateMemoryBankCommand;
import com.example.factoryguard.application.dto.ai.GenerateMemoryBankResult;
import com.example.factoryguard.application.exception.ai.AiServerException;
import com.example.factoryguard.application.port.out.ai.GenerateMemoryBankPort;
import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryBankFastApiAdapter implements GenerateMemoryBankPort {

    private final FastApiClient fastApiClient;
    private final RestTemplate restTemplate;

    @Override
    public GenerateMemoryBankResult generateMemoryBank(GenerateMemoryBankCommand command) throws TimeoutException {
        String url = fastApiClient.baseUrl() + "/ai/v1/internal/models/memory-bank";
        GenerateMemoryBankRequest request = GenerateMemoryBankRequest.builder()
                .modelCategory(command.getModelCategory().name())
                .modelProfile(command.getModelProfile().name())
                .normalImageFileKeys(command.getNormalImageFileKeys())
                .configFileKey(command.getConfigFileKey())
                .ckptFileKey(command.getCkptFileKey())
                .outputPrefix(command.getOutputPrefix())
                .build();
        try {
            GenerateMemoryBankResponse response = restTemplate.postForObject(url, request, GenerateMemoryBankResponse.class);
            return toResult(response);
        } catch (ResourceAccessException exception) {
            if (hasCause(exception, SocketTimeoutException.class)) {
                throw (TimeoutException) new TimeoutException(exception.getMessage()).initCause(exception);
            }
            throw new AiServerException("AI server connection failed: " + exception.getMessage(), exception);
        }
    }

    private GenerateMemoryBankResult toResult(GenerateMemoryBankResponse response) {
        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new AiServerException(0, "AI server returned empty memory_bank response");
        }
        GenerateMemoryBankResponse.MemoryBankData data = response.getData();
        return GenerateMemoryBankResult.builder()
                .memoryBankFileKey(data.getMemoryBankFileKey())
                .normalImageCount(data.getNormalImageCount())
                .modelCategory(ModelCategory.valueOf(data.getModelCategory()))
                .modelProfile(ModelProfile.valueOf(data.getModelProfile()))
                .createdAt(data.getCreatedAt())
                .build();
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
