package com.example.factoryguard.application.port.in.chat;

import com.example.factoryguard.application.dto.chat.ChatConversationSummary;
import com.example.factoryguard.application.dto.chat.ListChatConversationsQuery;

import java.util.List;

public interface ListChatConversationsUseCase {

    List<ChatConversationSummary> execute(ListChatConversationsQuery query);
}
