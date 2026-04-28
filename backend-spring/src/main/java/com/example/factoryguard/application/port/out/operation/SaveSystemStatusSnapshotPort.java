package com.example.factoryguard.application.port.out.operation;

import com.example.factoryguard.domain.operation.model.SystemStatusSnapshot;

public interface SaveSystemStatusSnapshotPort {

    SystemStatusSnapshot save(SystemStatusSnapshot snapshot);
}
