package com.example.factoryguard.application.port.out.file;

import com.example.factoryguard.domain.file.model.StoredFile;

public interface FileStoragePort {

    byte[] load(StoredFile file);
}
