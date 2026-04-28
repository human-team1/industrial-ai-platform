package com.example.factoryguard.application.dto.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AskResultBasedQuestionCommand {

    private final Long userId;
    private final Long resultId;
    private final Long conversationId;
    private final String question;
}
