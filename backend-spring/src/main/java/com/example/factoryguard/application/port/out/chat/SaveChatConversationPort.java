package com.example.factoryguard.application.port.out.chat;

import com.example.factoryguard.domain.chat.model.ChatConversation;

public interface SaveChatConversationPort {

    ChatConversation save(ChatConversation conversation);
}
