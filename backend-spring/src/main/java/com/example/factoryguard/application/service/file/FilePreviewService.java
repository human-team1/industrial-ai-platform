package com.example.factoryguard.application.service.file;

import com.example.factoryguard.application.dto.file.FilePreviewResult;
import com.example.factoryguard.application.port.in.file.GetFilePreviewUseCase;
import com.example.factoryguard.application.port.out.file.FileStoragePort;
import com.example.factoryguard.application.port.out.file.LoadFilePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.SecurityUtils;
import com.example.factoryguard.domain.file.model.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilePreviewService implements GetFilePreviewUseCase {

    private final LoadFilePort loadFilePort;
    private final FileStoragePort fileStoragePort;
    private final SecurityUtils securityUtils;

    @Override
    public FilePreviewResult execute(Long fileId) {
        if (fileId == null || fileId < 1) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "fileId는 1 이상의 값이어야 합니다.");
        }
        securityUtils.getCurrentPrincipal();

        StoredFile file = loadFilePort.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "파일을 찾을 수 없습니다."));

        return FilePreviewResult.builder()
                .content(fileStoragePort.load(file))
                .contentType(file.getMimeType() != null ? file.getMimeType() : "application/octet-stream")
                .build();
    }
}
