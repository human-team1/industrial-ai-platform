package com.example.factoryguard.application.dto.chat;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatConversationPageResult {

    private final List<ChatConversationSummary> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
}
