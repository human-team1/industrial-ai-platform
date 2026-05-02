package com.example.factoryguard.adapter.out.redis.operation;

import com.example.factoryguard.adapter.out.cache.redis.RedisCacheAdapter;
import com.example.factoryguard.application.dto.operation.SystemComponentStatusResult;
import com.example.factoryguard.application.port.out.operation.OperationStatusCachePort;
import com.example.factoryguard.config.operation.OperationMonitoringProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationStatusRedisAdapter implements OperationStatusCachePort {

    private static final String SYSTEM_KEY_PREFIX = "operation:system-status:";
    private static final String COMPONENT_KEY_PREFIX = "operation:component-status:";

    private final RedisCacheAdapter redisCacheAdapter;
    private final ObjectMapper objectMapper;
    private final OperationMonitoringProperties properties;

    @Override
    public Optional<SystemComponentStatusResult> findSystemStatus(String nodeType) {
        return read(SYSTEM_KEY_PREFIX + nodeType.toLowerCase() + ":latest");
    }

    @Override
    public Optional<SystemComponentStatusResult> findComponentStatus(String componentType) {
        return read(COMPONENT_KEY_PREFIX + componentType);
    }

    @Override
    public List<SystemComponentStatusResult> findComponentStatuses(List<String> componentTypes) {
        return componentTypes.stream()
                .map(this::findComponentStatus)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public void saveSystemStatus(String nodeType, SystemComponentStatusResult status) {
        write(SYSTEM_KEY_PREFIX + nodeType.toLowerCase() + ":latest", status);
    }

    @Override
    public void saveComponentStatus(SystemComponentStatusResult status) {
        write(COMPONENT_KEY_PREFIX + status.getComponentType(), status);
    }

    private Optional<SystemComponentStatusResult> read(String key) {
        try {
            return redisCacheAdapter.get(key)
                    .map(value -> {
                        try {
                            JsonNode node = objectMapper.readTree(value);
                            return SystemComponentStatusResult.builder()
                                    .componentStatusId(longValue(node, "componentStatusId"))
                                    .componentType(textValue(node, "componentType"))
                                    .componentName(textValue(node, "componentName"))
                                    .status(textValue(node, "status"))
                                    .message(textValue(node, "message"))
                                    .cpuUsage(decimalValue(node, "cpuUsage"))
                                    .memoryUsage(decimalValue(node, "memoryUsage"))
                                    .diskUsage(decimalValue(node, "diskUsage"))
                                    .hostName(textValue(node, "hostName"))
                                    .instanceId(textValue(node, "instanceId"))
                                    .responseTimeMs(intValue(node, "responseTimeMs"))
                                    .checkedAt(dateTimeValue(node, "checkedAt"))
                                    .createdAt(dateTimeValue(node, "createdAt"))
                                    .build();
                        } catch (Exception exception) {
                            log.warn("Failed to deserialize operation status cache, key={}", key, exception);
                            return null;
                        }
                    });
        } catch (Exception exception) {
            log.warn("Failed to read operation status cache, key={}", key, exception);
            return Optional.empty();
        }
    }

    private void write(String key, SystemComponentStatusResult status) {
        try {
            redisCacheAdapter.set(
                    key,
                    objectMapper.writeValueAsString(status),
                    Duration.ofSeconds(properties.getRedisTtlSeconds())
            );
        } catch (Exception exception) {
            log.warn("Failed to write operation status cache, key={}", key, exception);
        }
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Long longValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asLong();
    }

    private Integer intValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asInt();
    }

    private java.math.BigDecimal decimalValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.decimalValue();
    }

    private java.time.LocalDateTime dateTimeValue(JsonNode node, String field) {
        String value = textValue(node, field);
        return value == null || value.isBlank() ? null : java.time.LocalDateTime.parse(value);
    }
}
