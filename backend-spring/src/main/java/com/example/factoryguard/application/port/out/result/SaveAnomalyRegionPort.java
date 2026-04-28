package com.example.factoryguard.application.port.out.result;

import com.example.factoryguard.domain.result.model.AnomalyRegion;

public interface SaveAnomalyRegionPort {

    AnomalyRegion save(AnomalyRegion region);
}
