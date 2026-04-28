package com.example.factoryguard.application.dto.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AskDocumentQuestionCommand {

    private final Long userId;
    private final Long organizationId;
    private final Long conversationId;
    private final String question;
}
