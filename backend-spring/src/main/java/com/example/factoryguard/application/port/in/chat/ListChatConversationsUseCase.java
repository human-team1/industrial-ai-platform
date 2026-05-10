package com.example.factoryguard.application.port.in.chat;

import com.example.factoryguard.application.dto.chat.ChatConversationPageResult;
import com.example.factoryguard.application.dto.chat.ListChatConversationsQuery;

public interface ListChatConversationsUseCase {

    ChatConversationPageResult execute(ListChatConversationsQuery query);
}
