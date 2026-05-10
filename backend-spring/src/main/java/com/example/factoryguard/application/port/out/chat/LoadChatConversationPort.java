package com.example.factoryguard.application.port.out.chat;

import com.example.factoryguard.domain.chat.model.ChatConversation;
import com.example.factoryguard.application.dto.chat.ChatConversationPageResult;
import com.example.factoryguard.application.dto.chat.ListChatConversationsQuery;

import java.util.List;
import java.util.Optional;

public interface LoadChatConversationPort {

    Optional<ChatConversation> findById(Long conversationId);

    List<ChatConversation> findAllByUserId(Long userId);

    ChatConversationPageResult findPageByUserId(ListChatConversationsQuery query);
}
