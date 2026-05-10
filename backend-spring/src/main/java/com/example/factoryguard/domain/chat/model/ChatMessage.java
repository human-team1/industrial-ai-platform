package com.example.factoryguard.domain.chat.model;

import com.example.factoryguard.domain.chat.vo.ChatMessageRole;
import com.example.factoryguard.domain.chat.vo.ChatAnswerStatus;
import com.example.factoryguard.domain.chat.vo.ChatMessageStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessage {

    private final Long messageId;
    private final Long conversationId;
    private final ChatMessageRole role;
    private final String messageText;
    private final ChatMessageStatus messageStatus;
    private final ChatAnswerStatus answerStatus;
    private final String errorCode;
    private final String modelName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
