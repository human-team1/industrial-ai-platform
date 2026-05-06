package com.example.factoryguard.adapter.out.fastapi;

import com.example.factoryguard.adapter.out.fastapi.client.FastApiClient;
import com.example.factoryguard.adapter.out.fastapi.request.GenerateMemoryBankRequest;
import com.example.factoryguard.adapter.out.fastapi.response.GenerateMemoryBankResponse;
import com.example.factoryguard.application.dto.ai.GenerateMemoryBankCommand;
import com.example.factoryguard.application.dto.ai.GenerateMemoryBankResult;
import com.example.factoryguard.application.exception.ai.AiInvalidRequestException;
import com.example.factoryguard.application.exception.ai.AiServerException;
import com.example.factoryguard.application.port.out.ai.GenerateMemoryBankPort;
import com.example.factoryguard.config.client.AiServerProperties;
import com.example.factoryguard.domain.model.vo.ModelCategory;
import com.example.factoryguard.domain.model.vo.ModelProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryBankFastApiAdapter implements GenerateMemoryBankPort {

    private final FastApiClient fastApiClient;
    private final RestTemplate restTemplate;
    private final AiServerProperties aiServerProperties;

    @Override
    public GenerateMemoryBankResult generateMemoryBank(GenerateMemoryBankCommand command) throws TimeoutException {
        String url = fastApiClient.baseUrl() + "/ai/v1/internal/models/memory-bank";
        GenerateMemoryBankRequest request = GenerateMemoryBankRequest.builder()
                .modelCategory(command.getModelCategory().name())
                .modelProfile(command.getModelProfile().name())
                .ckptFileKey(command.getCkptFileKey())
                .configFileKey(command.getConfigFileKey())
                .normalImageFileKeys(command.getNormalImageFileKeys())
                .outputPrefix(command.getOutputPrefix())
                .build();
        Instant startedAt = Instant.now();
        try {
            log.info(
                    "memory_bank_fastapi_request_start requestId={} modelId={} organizationId={} targetId={} deploymentScope={} modelCategory={} modelProfile={} normalImageCount={} ckptFileKey={} configFileKey={} outputPrefix={} aiServerBaseUrl={} finalFastApiUrl={} connectTimeoutSeconds={} readTimeoutSeconds={}",
                    command.getRequestId(),
                    command.getModelId(),
                    command.getOrganizationId(),
                    command.getTargetId(),
                    command.getDeploymentScope(),
                    command.getModelCategory(),
                    command.getModelProfile(),
                    command.getNormalImageFileKeys() == null ? 0 : command.getNormalImageFileKeys().size(),
                    command.getCkptFileKey(),
                    command.getConfigFileKey(),
                    command.getOutputPrefix(),
                    fastApiClient.baseUrl(),
                    url,
                    millisToSeconds(resolveConnectTimeoutMs()),
                    millisToSeconds(resolveReadTimeoutMs())
            );
            GenerateMemoryBankResponse response = restTemplate.postForObject(url, withRequestId(request, command.getRequestId()), GenerateMemoryBankResponse.class);
            GenerateMemoryBankResult result = toResult(response);
            log.info(
                    "memory_bank_fastapi_request_success requestId={} modelCategory={} modelProfile={} elapsedMs={} memoryBankFileKey={} configFileKey={} ckptFileKey={}",
                    command.getRequestId(),
                    command.getModelCategory(),
                    command.getModelProfile(),
                    elapsedMs(startedAt),
                    result.getMemoryBankFileKey(),
                    result.getConfigFileKey(),
                    result.getCkptFileKey()
            );
            return result;
        } catch (ResourceAccessException exception) {
            if (hasCause(exception, SocketTimeoutException.class)) {
                log.warn(
                        "memory_bank_fastapi_request_timeout requestId={} modelCategory={} modelProfile={} elapsedMs={} timeoutType={} finalFastApiUrl={} errorCode={}",
                        command.getRequestId(),
                        command.getModelCategory(),
                        command.getModelProfile(),
                        elapsedMs(startedAt),
                        timeoutType(exception),
                        url,
                        "AI-504",
                        exception
                );
                throw (TimeoutException) new TimeoutException(exception.getMessage()).initCause(exception);
            }
            log.error(
                    "memory_bank_fastapi_request_failed requestId={} modelCategory={} modelProfile={} elapsedMs={} httpStatus={} upstreamErrorCode={} upstreamDetail={} finalFastApiUrl={}",
                    command.getRequestId(),
                    command.getModelCategory(),
                    command.getModelProfile(),
                    elapsedMs(startedAt),
                    0,
                    "AI_CONNECTION_FAILED",
                    exception.getMessage(),
                    url,
                    exception
            );
            throw new AiServerException("AI server connection failed: " + exception.getMessage(), exception);
        } catch (AiInvalidRequestException exception) {
            log.warn(
                    "memory_bank_fastapi_request_failed requestId={} modelCategory={} modelProfile={} elapsedMs={} httpStatus={} upstreamErrorCode={} upstreamDetail={} finalFastApiUrl={}",
                    command.getRequestId(),
                    command.getModelCategory(),
                    command.getModelProfile(),
                    elapsedMs(startedAt),
                    exception.getStatus(),
                    exception.getUpstreamErrorCode(),
                    exception.getUpstreamDetail(),
                    url,
                    exception
            );
            throw exception;
        } catch (AiServerException exception) {
            log.error(
                    "memory_bank_fastapi_request_failed requestId={} modelCategory={} modelProfile={} elapsedMs={} httpStatus={} upstreamErrorCode={} upstreamDetail={} finalFastApiUrl={}",
                    command.getRequestId(),
                    command.getModelCategory(),
                    command.getModelProfile(),
                    elapsedMs(startedAt),
                    exception.getStatus(),
                    exception.getUpstreamErrorCode(),
                    exception.getUpstreamDetail(),
                    url,
                    exception
            );
            throw exception;
        }
    }

    private HttpEntity<GenerateMemoryBankRequest> withRequestId(GenerateMemoryBankRequest request, String requestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (requestId != null && !requestId.isBlank()) {
            headers.set("X-Request-Id", requestId);
        }
        return new HttpEntity<>(request, headers);
    }

    private GenerateMemoryBankResult toResult(GenerateMemoryBankResponse response) {
        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new AiServerException(0, "AI server returned empty memory_bank response");
        }
        GenerateMemoryBankResponse.MemoryBankData data = response.getData();
        return GenerateMemoryBankResult.builder()
                .memoryBankFileKey(data.getMemoryBankFileKey())
                .configFileKey(data.getConfigFileKey())
                .ckptFileKey(data.getCkptFileKey())
                .normalImageCount(data.getNormalImageCount())
                .modelCategory(ModelCategory.valueOf(data.getModelCategory()))
                .modelProfile(ModelProfile.valueOf(data.getModelProfile()))
                .inputSize(data.getInputSize())
                .framework(data.getFramework())
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

    private String timeoutType(Throwable throwable) {
        if (hasCause(throwable, ConnectException.class)) {
            return "CONNECT_TIMEOUT";
        }
        String message = throwable.getMessage();
        if (message != null && message.toLowerCase().contains("read timed out")) {
            return "READ_TIMEOUT";
        }
        if (message != null && message.toLowerCase().contains("connect timed out")) {
            return "CONNECT_TIMEOUT";
        }
        return "UNKNOWN_TIMEOUT";
    }

    private long elapsedMs(Instant startedAt) {
        return Duration.between(startedAt, Instant.now()).toMillis();
    }

    private long resolveConnectTimeoutMs() {
        return aiServerProperties.getConnectTimeoutMs() > 0
                ? aiServerProperties.getConnectTimeoutMs()
                : aiServerProperties.getConnectTimeoutSec() * 1000L;
    }

    private long resolveReadTimeoutMs() {
        return aiServerProperties.getReadTimeoutMs() > 0
                ? aiServerProperties.getReadTimeoutMs()
                : aiServerProperties.getReadTimeoutSec() * 1000L;
    }

    private long millisToSeconds(long millis) {
        return Math.max(1L, millis / 1000L);
    }
}
