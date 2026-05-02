package com.example.factoryguard.application.port.in.operation;

import com.example.factoryguard.application.dto.operation.SystemComponentStatusResult;

import java.util.List;

public interface ListSystemComponentsUseCase {

    List<SystemComponentStatusResult> executeComponents();
}
