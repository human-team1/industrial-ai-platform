package com.example.factoryguard.application.service.operation;

import com.example.factoryguard.application.dto.operation.SystemComponentStatusResult;
import com.example.factoryguard.application.port.out.operation.OperationAdminPort;
import com.example.factoryguard.application.port.out.operation.OperationStatusCachePort;
import com.example.factoryguard.config.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationAdminServiceTest {

    @Mock private OperationAdminPort operationAdminPort;
    @Mock private OperationStatusCachePort operationStatusCachePort;
    @Mock private SecurityUtils securityUtils;

    private OperationAdminService service;

    @BeforeEach
    void setUp() {
        service = new OperationAdminService(operationAdminPort, operationStatusCachePort, securityUtils);
        when(securityUtils.isSiteAdmin()).thenReturn(true);
    }

    @Test
    void executeComponentsAddsUnknownFallbackForMinioAndChroma() {
        when(operationStatusCachePort.findComponentStatuses(List.of(
                "SPRING_API", "AI_SERVER", "MARIADB", "REDIS", "MINIO", "CHROMA", "STREAM_SERVER", "STORAGE"
        ))).thenReturn(List.of());
        when(operationAdminPort.findSystemComponents()).thenReturn(List.of());

        List<SystemComponentStatusResult> result = service.executeComponents();

        assertThat(result).extracting(SystemComponentStatusResult::getComponentType)
                .contains("SPRING_API", "AI_SERVER", "MARIADB", "REDIS", "MINIO", "CHROMA");
        assertThat(result).filteredOn(item -> "MINIO".equals(item.getComponentType()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getComponentName()).isEqualTo("MinIO");
                    assertThat(item.getStatus()).isEqualTo("UNKNOWN");
                    assertThat(item.getMessage()).isEqualTo("상태 정보 없음");
                });
        assertThat(result).filteredOn(item -> "CHROMA".equals(item.getComponentType()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getComponentName()).isEqualTo("ChromaDB");
                    assertThat(item.getStatus()).isEqualTo("UNKNOWN");
                    assertThat(item.getMessage()).isEqualTo("상태 정보 없음");
                });
    }
}
