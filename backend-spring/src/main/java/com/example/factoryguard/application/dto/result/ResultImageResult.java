package com.example.factoryguard.application.dto.result;

import com.example.factoryguard.domain.result.vo.ImageRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultImageResult {

    private final Long imageId;
    private final Long resultId;
    private final Long fileId;
    private final ImageRole imageRole;
}
