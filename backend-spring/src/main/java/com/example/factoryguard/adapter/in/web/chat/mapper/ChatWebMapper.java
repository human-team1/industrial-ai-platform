package com.example.factoryguard.adapter.in.web.chat.mapper;

import com.example.factoryguard.adapter.in.web.chat.dto.AskChatRequest;
import com.example.factoryguard.adapter.in.web.chat.dto.CreateChatConversationRequest;
import com.example.factoryguard.application.dto.chat.AskChatCommand;
import com.example.factoryguard.application.dto.chat.CreateChatConversationCommand;
import com.example.factoryguard.application.dto.chat.DocumentScope;
import com.example.factoryguard.application.dto.chat.ListChatConversationsQuery;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ChatWebMapper {

    public ListChatConversationsQuery toListQuery(
            AuthenticatedPrincipal principal,
            int page,
            int size,
            String keyword,
            LocalDate from,
            LocalDate to
    ) {
        return new ListChatConversationsQuery(principal.userId(), Math.max(page, 0), normalizeSize(size), keyword, from, to);
    }

    public CreateChatConversationCommand toCreateConversationCommand(
            AuthenticatedPrincipal principal,
            CreateChatConversationRequest request
    ) {
        return CreateChatConversationCommand.builder()
                .userId(principal.userId())
                .title(request == null ? null : request.getTitle())
                .build();
    }

    public AskChatCommand toAskChatCommand(
            AuthenticatedPrincipal principal,
            Long conversationId,
            AskChatRequest request
    ) {
        AskChatRequest safeRequest = request == null ? new AskChatRequest() : request;
        AskChatRequest.ChatMessageContext context = safeRequest.getContext() == null
                ? new AskChatRequest.ChatMessageContext()
                : safeRequest.getContext();

        return AskChatCommand.builder()
                .userId(principal.userId())
                .organizationId(principal.organizationId())
                .conversationId(conversationId)
                .question(safeRequest.getMessageText())
                .documentScope(parseScope(context.getDocumentScope()))
                .documentIds(context.getDocumentIds())
                .resultId(context.getResultId())
                .build();
    }

    private DocumentScope parseScope(String value) {
        try {
            return DocumentScope.valueOf(value == null ? "ALL" : value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "documentScope는 ALL 또는 SELECTED만 허용됩니다.");
        }
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
