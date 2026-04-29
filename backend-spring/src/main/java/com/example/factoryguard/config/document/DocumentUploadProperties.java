package com.example.factoryguard.config.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.document.upload")
public class DocumentUploadProperties {

    private long maxSizeBytes = 50L * 1024L * 1024L;
    private List<String> allowedExtensions = List.of("pdf", "docx", "txt");
    private List<String> allowedMimeTypes = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );
    private String objectPrefix = "documents";
}
