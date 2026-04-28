package com.example.factoryguard.application.port.out.chat;

import com.example.factoryguard.domain.chat.model.ChatConversation;

import java.util.List;
import java.util.Optional;

public interface LoadChatConversationPort {

    Optional<ChatConversation> findById(Long conversationId);

    List<ChatConversation> findAllByUserId(Long userId);
}
