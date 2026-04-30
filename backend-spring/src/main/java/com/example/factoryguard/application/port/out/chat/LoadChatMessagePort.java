package com.example.factoryguard.application.port.out.chat;

import com.example.factoryguard.domain.chat.model.ChatMessage;

import java.util.List;
import java.util.Optional;

public interface LoadChatMessagePort {

    List<ChatMessage> findAllByConversationId(Long conversationId);

    Optional<ChatMessage> findMessageById(Long messageId);
}
