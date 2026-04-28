package com.example.factoryguard.application.port.out.result;

import com.example.factoryguard.domain.result.model.Image;

import java.util.List;

public interface LoadResultImagePort {

    List<Image> findAllByResultId(Long resultId);
}
