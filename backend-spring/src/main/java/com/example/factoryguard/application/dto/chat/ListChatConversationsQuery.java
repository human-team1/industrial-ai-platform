package com.example.factoryguard.application.dto.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class ListChatConversationsQuery {

    private final Long userId;
    private final int page;
    private final int size;
    private final String keyword;
    private final LocalDate from;
    private final LocalDate to;
}
