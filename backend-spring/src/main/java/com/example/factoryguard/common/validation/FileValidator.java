package com.example.factoryguard.common.validation;

import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class FileValidator {

    private final FileValidationProperties properties;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EMPTY);
        }
        if (file.getSize() > properties.getMaxSize().toBytes()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_SIZE);
        }
        String mime = file.getContentType();
        if (mime == null || !properties.getAllowedMimeTypes().contains(mime.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.INVALID_FILE_MIME);
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }
        if (original.contains("..") || original.contains("/") || original.contains("\\")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }
        String ext = extractExtension(original);
        if (ext == null || !properties.getAllowedExtensions().contains(ext)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
