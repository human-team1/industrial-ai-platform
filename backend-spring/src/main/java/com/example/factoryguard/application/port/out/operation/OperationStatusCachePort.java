package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.application.dto.operation.SystemComponentStatusResult;

import java.util.List;
import java.util.Optional;

public interface OperationStatusCachePort {

    Optional<SystemComponentStatusResult> findSystemStatus(String nodeType);

    Optional<SystemComponentStatusResult> findComponentStatus(String componentType);

    List<SystemComponentStatusResult> findComponentStatuses(List<String> componentTypes);

    void saveSystemStatus(String nodeType, SystemComponentStatusResult status);

    void saveComponentStatus(SystemComponentStatusResult status);
}
