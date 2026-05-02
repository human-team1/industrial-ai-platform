package com.example.factoryguard.adapter.out.storage.minio;

import com.example.factoryguard.application.port.out.file.FileStoragePort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.file.model.StoredFile;
import com.example.factoryguard.domain.file.vo.StorageType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class MinioFileStorageAdapter implements FileStoragePort {

    private final MinioStorageAdapter minioStorageAdapter;
    private final Environment environment;

    @Override
    public byte[] load(StoredFile file) {
        if (isLocalProfile() && file.getStorageType() == StorageType.LOCAL) {
            return placeholderSvg(file);
        }
        if (file.getBucketName() == null || file.getObjectKey() == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "저장소 파일을 찾을 수 없습니다.");
        }
        try {
            return minioStorageAdapter.download(file.getBucketName(), file.getObjectKey());
        } catch (NoSuchElementException exception) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "파일 객체를 찾을 수 없습니다.");
        } catch (IllegalStateException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "파일 저장소 조회 중 오류가 발생했습니다.");
        }
    }

    private boolean isLocalProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("local");
    }

    private byte[] placeholderSvg(StoredFile file) {
        String label = file.getFileName() != null ? file.getFileName() : "sample-preview";
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="960" height="540" viewBox="0 0 960 540">
                  <rect width="960" height="540" fill="#f1f5f9"/>
                  <rect x="90" y="70" width="780" height="400" rx="24" fill="#ffffff" stroke="#cbd5e1" stroke-width="3"/>
                  <circle cx="320" cy="250" r="86" fill="#fee2e2" stroke="#ef4444" stroke-width="8"/>
                  <path d="M620 170l80 160H540z" fill="#fed7aa" stroke="#f97316" stroke-width="8"/>
                  <text x="480" y="420" text-anchor="middle" font-family="Arial, sans-serif" font-size="34" fill="#334155">%s</text>
                </svg>
                """.formatted(label);
        return svg.getBytes(StandardCharsets.UTF_8);
    }
}
