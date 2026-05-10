package com.example.factoryguard.domain.result.model;

import com.example.factoryguard.domain.result.vo.ImageRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Image {

    private final Long imageId;
    private final Long resultId;
    private final Long fileId;
    private final ImageRole imageRole;
    private final LocalDateTime createdAt;
}
