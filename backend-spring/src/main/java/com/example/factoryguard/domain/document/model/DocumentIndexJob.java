package com.example.factoryguard.domain.document.model;

import com.example.factoryguard.domain.document.vo.DocumentIndexJobStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentIndexJob {

    private final Long jobId;
    private final Long documentVersionId;
    private final DocumentIndexJobStatus jobStatus;
    private final String errorMessage;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
}
