package com.example.factoryguard.application.port.out.result;

import com.example.factoryguard.domain.result.model.AnomalyRegion;

import java.util.List;

public interface LoadAnomalyRegionPort {

    List<AnomalyRegion> findAllByImageId(Long imageId);
}
