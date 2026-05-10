package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.SystemStatusSnapshot;

import java.util.Optional;

public interface LoadSystemStatusSnapshotPort {

    Optional<SystemStatusSnapshot> findLatest();
}
