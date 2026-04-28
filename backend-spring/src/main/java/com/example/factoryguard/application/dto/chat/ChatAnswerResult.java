package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatAnswerResult {

    private final Long conversationId;
    private final Long messageId;
    private final String answerText;
}
