package com.example.factoryguard.application.dto.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ListChatConversationsQuery {

    private final Long userId;
    private final int page;
    private final int size;
}
