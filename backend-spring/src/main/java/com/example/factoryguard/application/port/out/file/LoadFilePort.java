package com.example.factoryguard.application.port.out.file;

import com.example.factoryguard.domain.file.model.StoredFile;

import java.util.Optional;

public interface LoadFilePort {

    Optional<StoredFile> findById(Long fileId);

    Optional<StoredFile> findByObjectKey(String objectKey);
}
