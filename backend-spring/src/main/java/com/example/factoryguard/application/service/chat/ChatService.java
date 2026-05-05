package com.example.factoryguard.application.service.chat;

import com.example.factoryguard.application.dto.chat.AskChatCommand;
import com.example.factoryguard.application.dto.chat.AskChatResult;
import com.example.factoryguard.application.dto.chat.ChatConversationDetail;
import com.example.factoryguard.application.dto.chat.ChatConversationPageResult;
import com.example.factoryguard.application.dto.chat.ChatConversationResult;
import com.example.factoryguard.application.dto.chat.ChatMessageResult;
import com.example.factoryguard.application.dto.chat.ChatSourceResult;
import com.example.factoryguard.application.dto.chat.CreateChatConversationCommand;
import com.example.factoryguard.application.dto.chat.DocumentScope;
import com.example.factoryguard.application.dto.chat.ListChatConversationsQuery;
import com.example.factoryguard.application.dto.chat.RagAnswerRequest;
import com.example.factoryguard.application.dto.chat.RagAnswerResponse;
import com.example.factoryguard.application.dto.chat.RagAnswerSource;
import com.example.factoryguard.application.dto.chat.RagResultContext;
import com.example.factoryguard.application.dto.result.ResultDetailResponse;
import com.example.factoryguard.application.port.in.chat.AskChatUseCase;
import com.example.factoryguard.application.port.in.chat.CreateChatConversationUseCase;
import com.example.factoryguard.application.port.in.chat.DeleteChatConversationUseCase;
import com.example.factoryguard.application.port.in.chat.GetChatConversationUseCase;
import com.example.factoryguard.application.port.in.chat.ListChatConversationsUseCase;
import com.example.factoryguard.application.port.in.chat.ListChatMessagesUseCase;
import com.example.factoryguard.application.port.in.chat.ListChatSourcesUseCase;
import com.example.factoryguard.application.port.out.chat.DeleteChatConversationPort;
import com.example.factoryguard.application.port.out.chat.LoadChatConversationPort;
import com.example.factoryguard.application.port.out.chat.LoadChatMessagePort;
import com.example.factoryguard.application.port.out.chat.LoadChatSourcePort;
import com.example.factoryguard.application.port.out.chat.RequestRagAnswerPort;
import com.example.factoryguard.application.port.out.chat.SaveChatConversationPort;
import com.example.factoryguard.application.port.out.chat.SaveChatMessagePort;
import com.example.factoryguard.application.port.out.chat.SaveChatSourcePort;
import com.example.factoryguard.application.port.out.result.ResultQueryPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.chat.model.ChatConversation;
import com.example.factoryguard.domain.chat.model.ChatMessage;
import com.example.factoryguard.domain.chat.model.ChatSource;
import com.example.factoryguard.domain.chat.vo.ChatAnswerStatus;
import com.example.factoryguard.domain.chat.vo.ChatMessageRole;
import com.example.factoryguard.domain.chat.vo.ChatMessageStatus;
import com.example.factoryguard.domain.chat.vo.ChatSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService implements AskChatUseCase, CreateChatConversationUseCase, ListChatConversationsUseCase,
        GetChatConversationUseCase, ListChatMessagesUseCase, ListChatSourcesUseCase, DeleteChatConversationUseCase {

    private static final int MAX_QUESTION_LENGTH = 1000;
    private static final int TITLE_MAX_LENGTH = 60;
    private static final String NO_SOURCE_ANSWER =
            "참조 가능한 문서를 찾지 못했습니다. 설비명, 증상, 문서명을 더 구체적으로 입력해 주세요.";

    private final LoadChatConversationPort loadChatConversationPort;
    private final SaveChatConversationPort saveChatConversationPort;
    private final LoadChatMessagePort loadChatMessagePort;
    private final SaveChatMessagePort saveChatMessagePort;
    private final LoadChatSourcePort loadChatSourcePort;
    private final SaveChatSourcePort saveChatSourcePort;
    private final DeleteChatConversationPort deleteChatConversationPort;
    private final RequestRagAnswerPort requestRagAnswerPort;
    private final ResultQueryPort resultQueryPort;

    @Override
    @Transactional
    public AskChatResult execute(AskChatCommand command) {
        String question = normalizeQuestion(command.getQuestion());
        validateScope(command.getDocumentScope(), command.getDocumentIds());

        ChatConversation conversation = resolveConversation(command.getConversationId(), command.getUserId());
        ChatMessage userMessage = saveChatMessagePort.save(ChatMessage.builder()
                .conversationId(conversation.getConversationId())
                .role(ChatMessageRole.USER)
                .messageText(question)
                .messageStatus(ChatMessageStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build());

        RagAnswerResponse ragAnswer = requestRagAnswerPort.requestAnswer(RagAnswerRequest.builder()
                .userId(command.getUserId())
                .organizationId(command.getOrganizationId())
                .conversationId(conversation.getConversationId())
                .question(question)
                .documentScope(command.getDocumentScope())
                .documentIds(command.getDocumentIds())
                .resultId(command.getResultId())
                .resultContext(resolveResultContext(command))
                .build());

        ChatAnswerStatus answerStatus = normalizeAnswerStatus(ragAnswer);
        ChatMessage assistantMessage = saveChatMessagePort.save(ChatMessage.builder()
                .conversationId(conversation.getConversationId())
                .role(ChatMessageRole.ASSISTANT)
                .messageText(normalizeAnswer(ragAnswer, answerStatus))
                .messageStatus(normalizeMessageStatus(answerStatus))
                .answerStatus(answerStatus)
                .errorCode(ragAnswer == null ? "RAG_EMPTY_RESPONSE" : ragAnswer.getErrorCode())
                .modelName(ragAnswer == null ? null : ragAnswer.getModelName())
                .createdAt(LocalDateTime.now())
                .build());

        saveSources(assistantMessage.getMessageId(), ragAnswer == null ? List.of() : ragAnswer.getSources());
        List<ChatSourceResult> sources = loadChatSourcePort.findAllResultsByMessageId(assistantMessage.getMessageId());

        return AskChatResult.builder()
                .conversationId(conversation.getConversationId())
                .userMessage(toMessageResult(userMessage, List.of()))
                .assistantMessage(toMessageResult(assistantMessage, sources))
                .build();
    }

    @Override
    @Transactional
    public ChatConversationResult create(CreateChatConversationCommand command) {
        String title = command.getTitle() == null || command.getTitle().isBlank()
                ? "새 대화"
                : createTitle(command.getTitle().trim());
        ChatConversation conversation = saveChatConversationPort.save(ChatConversation.builder()
                .userId(command.getUserId())
                .title(title)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        return toConversationResult(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatConversationPageResult execute(ListChatConversationsQuery query) {
        return loadChatConversationPort.findPageByUserId(query);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatConversationDetail execute(Long userId, Long conversationId) {
        ChatConversation conversation = loadChatConversationPort.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "챗봇 대화를 찾을 수 없습니다."));
        assertOwner(conversation, userId);
        return ChatConversationDetail.builder()
                .conversationId(conversation.getConversationId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .messages(listMessages(userId, conversationId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResult> listMessages(Long userId, Long conversationId) {
        ChatConversation conversation = loadChatConversationPort.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "챗봇 대화를 찾을 수 없습니다."));
        assertOwner(conversation, userId);
        return loadChatMessagePort.findAllByConversationId(conversationId).stream()
                .map(message -> toMessageResult(message, loadChatSourcePort.findAllResultsByMessageId(message.getMessageId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatSourceResult> listSources(Long userId, Long messageId) {
        ChatMessage message = loadChatMessagePort.findMessageById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "챗봇 메시지를 찾을 수 없습니다."));
        ChatConversation conversation = loadChatConversationPort.findById(message.getConversationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "챗봇 대화를 찾을 수 없습니다."));
        assertOwner(conversation, userId);
        return loadChatSourcePort.findAllResultsByMessageId(messageId);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long conversationId) {
        ChatConversation conversation = loadChatConversationPort.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "챗봇 대화를 찾을 수 없습니다."));
        assertOwner(conversation, userId);
        boolean deleted = deleteChatConversationPort.softDelete(conversationId, userId);
        if (!deleted) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "챗봇 대화를 찾을 수 없습니다.");
        }
    }

    private ChatConversation resolveConversation(Long conversationId, Long userId) {
        if (conversationId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "conversationId가 필요합니다.");
        }
        ChatConversation conversation = loadChatConversationPort.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "챗봇 대화를 찾을 수 없습니다."));
        assertOwner(conversation, userId);
        return conversation;
    }

    private String normalizeQuestion(String question) {
        String normalized = question == null ? "" : question.trim();
        if (normalized.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "질문을 입력해 주세요.");
        }
        if (normalized.length() > MAX_QUESTION_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "질문은 1000자 이내로 입력해 주세요.");
        }
        return normalized;
    }

    private void validateScope(DocumentScope scope, List<Long> documentIds) {
        if (scope == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "검색 범위를 선택해 주세요.");
        }
        if (scope == DocumentScope.SELECTED && (documentIds == null || documentIds.isEmpty())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "선택 문서 검색에는 documentIds가 필요합니다.");
        }
    }

    private void saveSources(Long messageId, List<RagAnswerSource> ragSources) {
        if (ragSources == null || ragSources.isEmpty()) {
            return;
        }
        ragSources.forEach(source -> saveChatSourcePort.save(ChatSource.builder()
                .messageId(messageId)
                .sourceType(ChatSourceType.DOCUMENT)
                .sourceId(source.getSourceId() != null ? source.getSourceId() : source.getDocumentId())
                .documentId(source.getDocumentId())
                .documentTitle(source.getDocumentTitle())
                .documentType(source.getDocumentType())
                .chunkId(source.getChunkId())
                .pageNo(source.getPage())
                .section(source.getSection())
                .sourceSnippet(source.getSourceSnippet())
                .score(source.getScore())
                .createdAt(LocalDateTime.now())
                .build()));
    }

    private String normalizeAnswer(RagAnswerResponse ragAnswer, ChatAnswerStatus answerStatus) {
        if (answerStatus == ChatAnswerStatus.NO_RELEVANT_SOURCE) {
            return NO_SOURCE_ANSWER;
        }
        if (answerStatus != ChatAnswerStatus.ANSWERED) {
            return "챗봇 답변을 생성하지 못했습니다. 잠시 후 다시 시도해 주세요.";
        }
        if (ragAnswer == null || ragAnswer.getAnswerText() == null || ragAnswer.getAnswerText().isBlank()) {
            return NO_SOURCE_ANSWER;
        }
        return ragAnswer.getAnswerText().trim();
    }

    private ChatAnswerStatus normalizeAnswerStatus(RagAnswerResponse ragAnswer) {
        if (ragAnswer == null) {
            return ChatAnswerStatus.LLM_FAILED;
        }
        if (ragAnswer.getAnswerStatus() != null && ragAnswer.getAnswerStatus() != ChatAnswerStatus.ANSWERED) {
            return ragAnswer.getAnswerStatus();
        }
        if (ragAnswer.getSources() == null || ragAnswer.getSources().isEmpty()) {
            return ChatAnswerStatus.NO_RELEVANT_SOURCE;
        }
        return ChatAnswerStatus.ANSWERED;
    }

    private ChatMessageStatus normalizeMessageStatus(ChatAnswerStatus answerStatus) {
        return switch (answerStatus) {
            case ANSWERED, NO_RELEVANT_SOURCE -> ChatMessageStatus.SUCCESS;
            case LLM_FAILED, VECTOR_STORE_FAILED, DOCUMENT_SCOPE_FORBIDDEN, VALIDATION_FAILED -> ChatMessageStatus.FAILED;
            case OUT_OF_SCOPE -> ChatMessageStatus.SUCCESS;
        };
    }

    private RagResultContext resolveResultContext(AskChatCommand command) {
        if (command.getResultId() == null) {
            return null;
        }
        Long resultOrganizationId = resultQueryPort.findOrganizationIdByResultId(command.getResultId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "검사 결과를 찾을 수 없습니다."));
        if (!resultOrganizationId.equals(command.getOrganizationId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "다른 회사의 검사 결과에는 접근할 수 없습니다.");
        }
        ResultDetailResponse detail = resultQueryPort.findDetail(command.getResultId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "검사 결과를 찾을 수 없습니다."));
        return RagResultContext.builder()
                .resultId(detail.getResultId())
                .inspectionId(detail.getInspectionId())
                .decisionCode(detail.getResult() == null ? null : detail.getResult().getFinalDecisionCode())
                .score(detail.getResult() == null ? null : detail.getResult().getScore())
                .confidence(detail.getResult() == null ? null : detail.getResult().getConfidence())
                .equipmentName(detail.getTarget() == null ? null : detail.getTarget().getEquipmentName())
                .targetId(detail.getTarget() == null ? null : detail.getTarget().getTargetId())
                .anomalySummary(detail.getResult() == null ? null : detail.getResult().getFailureReason())
                .build();
    }

    private String createTitle(String question) {
        return question.length() <= TITLE_MAX_LENGTH ? question : question.substring(0, TITLE_MAX_LENGTH);
    }

    private void assertOwner(ChatConversation conversation, Long userId) {
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 챗봇 대화만 접근할 수 있습니다.");
        }
    }

    private ChatConversationResult toConversationResult(ChatConversation conversation) {
        return ChatConversationResult.builder()
                .conversationId(conversation.getConversationId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private ChatMessageResult toMessageResult(ChatMessage message, List<ChatSourceResult> sources) {
        return ChatMessageResult.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversationId())
                .role(message.getRole())
                .messageText(message.getMessageText())
                .messageStatus(message.getMessageStatus())
                .answerStatus(message.getAnswerStatus())
                .errorCode(message.getErrorCode())
                .modelName(message.getModelName())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .sources(sources)
                .build();
    }
}
