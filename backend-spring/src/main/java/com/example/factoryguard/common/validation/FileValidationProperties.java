package com.example.factoryguard.common.validation;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "inspection.upload")
public class FileValidationProperties {

    private DataSize maxSize = DataSize.ofMegabytes(50);
    private List<String> allowedMimeTypes = List.of(
            "image/jpeg", "image/png", "image/webp", "video/mp4"
    );
    private List<String> allowedExtensions = List.of(
            "jpg", "jpeg", "png", "webp", "mp4"
    );
}
