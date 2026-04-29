package com.example.factoryguard.application.port.in.file;

import com.example.factoryguard.application.dto.file.FilePreviewResult;

public interface GetFilePreviewUseCase {

    FilePreviewResult execute(Long fileId);
}
