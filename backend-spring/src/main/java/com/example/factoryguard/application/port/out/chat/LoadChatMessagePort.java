package com.example.factoryguard.application.port.out.chat;

import com.example.factoryguard.domain.chat.model.ChatMessage;

import java.util.List;

public interface LoadChatMessagePort {

    List<ChatMessage> findAllByConversationId(Long conversationId);
}
