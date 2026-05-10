package com.example.factoryguard.adapter.in.web.file;

import com.example.factoryguard.application.dto.file.FilePreviewResult;
import com.example.factoryguard.application.port.in.file.GetFilePreviewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final GetFilePreviewUseCase getFilePreviewUseCase;

    @GetMapping("/{fileId}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long fileId) {
        FilePreviewResult result = getFilePreviewUseCase.execute(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.getContentType()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES))
                .body(result.getContent());
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        FilePreviewResult result = getFilePreviewUseCase.execute(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .cacheControl(CacheControl.noCache())
                .body(result.getContent());
    }
}
