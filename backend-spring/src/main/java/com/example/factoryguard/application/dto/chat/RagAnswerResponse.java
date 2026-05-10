package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import com.example.factoryguard.domain.chat.vo.ChatAnswerStatus;

@Getter
@Builder
public class RagAnswerResponse {

    private final String answerText;
    private final ChatAnswerStatus answerStatus;
    private final String errorCode;
    private final String modelName;
    private final List<RagAnswerSource> sources;
}
