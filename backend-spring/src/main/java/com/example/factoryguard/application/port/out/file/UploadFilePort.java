package com.example.factoryguard.application.port.out.file;

import com.example.factoryguard.domain.file.model.StoredFile;

public interface UploadFilePort {

    StoredFile upload(StoredFile file);
}
