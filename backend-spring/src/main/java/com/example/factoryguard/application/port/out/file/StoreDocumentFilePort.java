package com.example.factoryguard.application.port.out.file;

import com.example.factoryguard.domain.file.model.StoredFile;

public interface StoreDocumentFilePort {

    StoredFile store(
            byte[] content,
            String originalFileName,
            String mimeType,
            String checksum,
            Long createdBy
    );
}
