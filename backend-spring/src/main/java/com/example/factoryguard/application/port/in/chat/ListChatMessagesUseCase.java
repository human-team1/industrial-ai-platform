package com.example.factoryguard.application.port.in.chat;

import com.example.factoryguard.application.dto.chat.ChatMessageResult;

import java.util.List;

public interface ListChatMessagesUseCase {

    List<ChatMessageResult> listMessages(Long userId, Long conversationId);
}
