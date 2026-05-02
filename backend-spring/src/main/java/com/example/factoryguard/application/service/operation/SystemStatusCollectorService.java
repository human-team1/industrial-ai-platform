package com.example.factoryguard.application.service.operation;

import com.example.factoryguard.adapter.out.cache.redis.RedisCacheAdapter;
import com.example.factoryguard.application.dto.operation.RecordOperationLogCommand;
import com.example.factoryguard.application.dto.operation.SystemComponentStatusResult;
import com.example.factoryguard.application.port.in.operation.RecordOperationLogUseCase;
import com.example.factoryguard.application.port.out.operation.OperationAdminPort;
import com.example.factoryguard.application.port.out.operation.OperationStatusCachePort;
import com.example.factoryguard.config.client.AiServerProperties;
import com.example.factoryguard.config.operation.OperationMonitoringProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.sql.Connection;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemStatusCollectorService {

    private static final String SOURCE = "SPRING_API";

    private final OperationAdminPort operationAdminPort;
    private final OperationStatusCachePort operationStatusCachePort;
    private final RecordOperationLogUseCase recordOperationLogUseCase;
    private final RedisCacheAdapter redisCacheAdapter;
    private final DataSource dataSource;
    private final RestTemplate restTemplate;
    private final AiServerProperties aiServerProperties;
    private final OperationMonitoringProperties properties;

    @Scheduled(fixedDelayString = "${app.operation.monitoring.collect-interval-ms:30000}")
    @Transactional
    public void collect() {
        SystemComponentStatusResult spring = collectSpringStatus();
        saveStatusHistory("spring", spring, true);

        saveStatusHistory(null, checkMariaDb(), false);
        saveStatusHistory(null, checkRedis(), false);
        saveStatusHistory("ai", checkAiServer(), false);
        saveStatusHistory(null, collectStorageStatus(), false);
        saveStatusHistory(null, unknown("MINIO", "MinIO", "별도 health client가 없어 UNKNOWN으로 표시합니다."), false);
        saveStatusHistory(null, unknown("CHROMA", "ChromaDB", "별도 heartbeat 설정이 없어 UNKNOWN으로 표시합니다."), false);
        saveStatusHistory(null, unknown("STREAM_SERVER", "Stream Server", "실시간 스트림 서버가 아직 연결되지 않았습니다."), false);
    }

    private SystemComponentStatusResult collectSpringStatus() {
        long start = System.nanoTime();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal cpu = cpuUsage();
        BigDecimal memory = memoryUsage();
        BigDecimal disk = diskUsage(new File("."));
        return SystemComponentStatusResult.builder()
                .componentType("SPRING_API")
                .componentName("Spring API 서버")
                .status(statusFromUsage(cpu, memory, disk))
                .message("Spring 서버 상태 수집 완료")
                .cpuUsage(cpu)
                .memoryUsage(memory)
                .diskUsage(disk)
                .hostName(hostName())
                .instanceId(properties.getInstanceId())
                .responseTimeMs(elapsedMs(start))
                .checkedAt(now)
                .createdAt(now)
                .build();
    }

    private SystemComponentStatusResult collectStorageStatus() {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal disk = diskUsage(new File("."));
        return SystemComponentStatusResult.builder()
                .componentType("STORAGE")
                .componentName("Storage")
                .status(statusFromUsage(null, null, disk))
                .message("로컬/컨테이너 파일시스템 기준 디스크 사용률")
                .diskUsage(disk)
                .hostName(hostName())
                .instanceId(properties.getInstanceId())
                .checkedAt(now)
                .createdAt(now)
                .build();
    }

    private SystemComponentStatusResult checkMariaDb() {
        long start = System.nanoTime();
        LocalDateTime now = LocalDateTime.now();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(1);
            return simple("MARIADB", "MariaDB", valid ? "NORMAL" : "ERROR",
                    valid ? "연결 정상" : "연결 검증 실패", elapsedMs(start), now);
        } catch (Exception exception) {
            recordFailure("MARIADB_HEALTH_CHECK_FAILED", "MariaDB 상태 확인 실패", exception);
            return simple("MARIADB", "MariaDB", "ERROR", "연결 실패", elapsedMs(start), now);
        }
    }

    private SystemComponentStatusResult checkRedis() {
        long start = System.nanoTime();
        LocalDateTime now = LocalDateTime.now();
        boolean ok = redisCacheAdapter.ping();
        if (!ok) {
            recordFailure("REDIS_HEALTH_CHECK_FAILED", "Redis 상태 확인 실패", null);
        }
        return simple("REDIS", "Redis", ok ? "NORMAL" : "ERROR", ok ? "PING 정상" : "PING 실패", elapsedMs(start), now);
    }

    private SystemComponentStatusResult checkAiServer() {
        long start = System.nanoTime();
        LocalDateTime now = LocalDateTime.now();
        String path = properties.getAiStatusPath();
        String url = aiServerProperties.getBaseUrl() + path;
        try {
            JsonNode root = restTemplate.getForObject(url, JsonNode.class);
            JsonNode data = root != null && root.has("data") ? root.get("data") : root;
            return SystemComponentStatusResult.builder()
                    .componentType("AI_SERVER")
                    .componentName(text(data, "nodeName", "AI 모델 서버"))
                    .status(text(data, "status", "UNKNOWN"))
                    .message(text(data, "message", "AI 서버 상태 응답 수신"))
                    .cpuUsage(decimal(data, "cpuUsage"))
                    .memoryUsage(decimal(data, "memoryUsage"))
                    .diskUsage(decimal(data, "diskUsage"))
                    .hostName(text(data, "hostName", null))
                    .instanceId(text(data, "instanceId", "ai-server"))
                    .responseTimeMs(integer(data, "responseTimeMs", elapsedMs(start)))
                    .checkedAt(now)
                    .createdAt(now)
                    .build();
        } catch (RestClientException exception) {
            recordFailure("AI_SERVER_HEALTH_CHECK_FAILED", "AI 서버 상태 API 호출 실패", exception);
            return simple("AI_SERVER", "AI 모델 서버", "ERROR", "AI 서버 상태 API 호출 실패", elapsedMs(start), now);
        }
    }

    private void saveStatusHistory(String nodeType, SystemComponentStatusResult status, boolean systemSnapshot) {
        try {
            if (nodeType != null) {
                operationStatusCachePort.saveSystemStatus(nodeType, status);
            }
            operationStatusCachePort.saveComponentStatus(status);
        } catch (Exception exception) {
            log.warn("Failed to save operation status cache, componentType={}", status.getComponentType(), exception);
        }
        try {
            if (systemSnapshot) {
                operationAdminPort.saveSystemStatusSnapshot(status);
            }
            operationAdminPort.saveSystemComponentStatus(status);
        } catch (Exception exception) {
            log.warn("Failed to save operation status history, componentType={}", status.getComponentType(), exception);
            recordFailure("SYSTEM_STATUS_COLLECT_FAILED", "시스템 상태 이력 저장 실패", exception);
        }
    }

    private SystemComponentStatusResult simple(String type, String name, String status, String message, Integer responseTimeMs, LocalDateTime checkedAt) {
        return SystemComponentStatusResult.builder()
                .componentType(type)
                .componentName(name)
                .status(status)
                .message(message)
                .hostName(hostName())
                .instanceId(properties.getInstanceId())
                .responseTimeMs(responseTimeMs)
                .checkedAt(checkedAt)
                .createdAt(checkedAt)
                .build();
    }

    private SystemComponentStatusResult unknown(String type, String name, String message) {
        LocalDateTime now = LocalDateTime.now();
        return simple(type, name, "UNKNOWN", message, null, now);
    }

    private void recordFailure(String eventType, String message, Exception exception) {
        if (exception != null) {
            log.warn("{}, requestId={}, reason={}", message, org.slf4j.MDC.get("requestId"), exception.getMessage());
        }
        recordOperationLogUseCase.recordOperationLog(RecordOperationLogCommand.builder()
                .eventType(eventType)
                .eventStatus("FAILED")
                .logLevel("ERROR")
                .sourceComponent(SOURCE)
                .detailMessage(message)
                .relatedPath("/api/v1/admin/system-components")
                .build());
    }

    private BigDecimal cpuUsage() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean osBean) {
            double value = osBean.getCpuLoad();
            if (value >= 0) {
                return percent(value * 100);
            }
        }
        return null;
    }

    private BigDecimal memoryUsage() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean osBean) {
            long total = osBean.getTotalMemorySize();
            long free = osBean.getFreeMemorySize();
            if (total > 0) {
                return percent(((double) (total - free) / total) * 100);
            }
        }
        return null;
    }

    private BigDecimal diskUsage(File base) {
        File root = base.getAbsoluteFile();
        long total = root.getTotalSpace();
        long free = root.getFreeSpace();
        if (total <= 0) {
            return null;
        }
        return percent(((double) (total - free) / total) * 100);
    }

    private String statusFromUsage(BigDecimal cpu, BigDecimal memory, BigDecimal disk) {
        BigDecimal max = java.util.stream.Stream.of(cpu, memory, disk)
                .filter(java.util.Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        if (max.compareTo(BigDecimal.valueOf(90)) >= 0) return "ERROR";
        if (max.compareTo(BigDecimal.valueOf(80)) >= 0) return "WARNING";
        return "NORMAL";
    }

    private BigDecimal percent(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private Integer elapsedMs(long startNanos) {
        return Math.max(0, (int) ((System.nanoTime() - startNanos) / 1_000_000));
    }

    private String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            return "unknown-host";
        }
    }

    private String text(JsonNode node, String field, String fallback) {
        return node != null && node.hasNonNull(field) ? node.get(field).asText() : fallback;
    }

    private BigDecimal decimal(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) ? BigDecimal.valueOf(node.get(field).asDouble()).setScale(2, RoundingMode.HALF_UP) : null;
    }

    private Integer integer(JsonNode node, String field, Integer fallback) {
        return node != null && node.hasNonNull(field) ? node.get(field).asInt() : fallback;
    }
}
