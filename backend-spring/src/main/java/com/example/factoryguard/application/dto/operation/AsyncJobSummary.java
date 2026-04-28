package com.example.factoryguard.application.dto.operation;

import com.example.factoryguard.domain.operation.vo.AsyncJobStatus;
import com.example.factoryguard.domain.operation.vo.AsyncJobType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AsyncJobSummary {

    private final Long jobId;
    private final AsyncJobType jobType;
    private final AsyncJobStatus jobStatus;
    private final String targetType;
    private final Long targetId;
}
