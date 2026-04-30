package com.example.factoryguard.adapter.in.web.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AskChatRequest {

    private String messageText;
    private ChatMessageContext context = new ChatMessageContext();

    @Getter
    @Setter
    public static class ChatMessageContext {
        private String documentScope = "ALL";
        private List<Long> documentIds = List.of();
        private Long resultId;
    }
}
