package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RagAnswerRequest {

    private final Long userId;
    private final Long organizationId;
    private final String question;
    private final DocumentScope documentScope;
    private final List<Long> documentIds;
    private final Long resultId;
}
