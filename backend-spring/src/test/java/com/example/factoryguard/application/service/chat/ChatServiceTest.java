package com.example.factoryguard.application.service.chat;

import com.example.factoryguard.application.dto.chat.AskChatCommand;
import com.example.factoryguard.application.dto.chat.AskChatResult;
import com.example.factoryguard.application.dto.chat.DocumentScope;
import com.example.factoryguard.application.dto.chat.RagAnswerResponse;
import com.example.factoryguard.application.port.out.chat.DeleteChatConversationPort;
import com.example.factoryguard.application.port.out.chat.LoadChatConversationPort;
import com.example.factoryguard.application.port.out.chat.LoadChatMessagePort;
import com.example.factoryguard.application.port.out.chat.LoadChatSourcePort;
import com.example.factoryguard.application.port.out.chat.RequestRagAnswerPort;
import com.example.factoryguard.application.port.out.chat.SaveChatConversationPort;
import com.example.factoryguard.application.port.out.chat.SaveChatMessagePort;
import com.example.factoryguard.application.port.out.chat.SaveChatSourcePort;
import com.example.factoryguard.application.port.out.document.LoadVectorIndexPort;
import com.example.factoryguard.application.port.out.result.ResultQueryPort;
import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.domain.chat.model.ChatConversation;
import com.example.factoryguard.domain.chat.model.ChatMessage;
import com.example.factoryguard.domain.chat.vo.ChatAnswerStatus;
import com.example.factoryguard.domain.chat.vo.ChatMessageStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private LoadChatConversationPort loadChatConversationPort;
    @Mock private SaveChatConversationPort saveChatConversationPort;
    @Mock private LoadChatMessagePort loadChatMessagePort;
    @Mock private SaveChatMessagePort saveChatMessagePort;
    @Mock private LoadChatSourcePort loadChatSourcePort;
    @Mock private SaveChatSourcePort saveChatSourcePort;
    @Mock private DeleteChatConversationPort deleteChatConversationPort;
    @Mock private RequestRagAnswerPort requestRagAnswerPort;
    @Mock private ResultQueryPort resultQueryPort;
    @Mock private LoadVectorIndexPort loadVectorIndexPort;

    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("No.30 빈 질문(공백) 차단 - VALIDATION_FAILED")
    void blankQuestionRejected() {
        AskChatCommand command = AskChatCommand.builder()
                .userId(1L).organizationId(100L).conversationId(50L)
                .question("   ").documentScope(DocumentScope.ALL).build();

        assertThatThrownBy(() -> chatService.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_FAILED);

        verify(requestRagAnswerPort, never()).requestAnswer(any());
        verify(saveChatMessagePort, never()).save(any());
    }

    @Test
    @DisplayName("No.30 null 질문 차단 - VALIDATION_FAILED")
    void nullQuestionRejected() {
        AskChatCommand command = AskChatCommand.builder()
                .userId(1L).organizationId(100L).conversationId(50L)
                .question(null).documentScope(DocumentScope.ALL).build();

        assertThatThrownBy(() -> chatService.execute(command))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("No.30 1000자 초과 질문 차단 - VALIDATION_FAILED")
    void oversizedQuestionRejected() {
        AskChatCommand command = AskChatCommand.builder()
                .userId(1L).organizationId(100L).conversationId(50L)
                .question("a".repeat(1001)).documentScope(DocumentScope.ALL).build();

        assertThatThrownBy(() -> chatService.execute(command))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("No.32 관련 문서 없음 - sources가 비면 NO_RELEVANT_SOURCE 안내 메시지로 응답")
    void noRelevantSourceProducesNoSourceAnswer() {
        long userId = 1L;
        long conversationId = 50L;
        ChatConversation conv = ChatConversation.builder()
                .conversationId(conversationId).userId(userId).title("t").build();
        when(loadChatConversationPort.findById(conversationId)).thenReturn(Optional.of(conv));

        AtomicLong messageIdSeq = new AtomicLong(7000L);
        when(saveChatMessagePort.save(any())).thenAnswer(invocation -> {
            ChatMessage in = invocation.getArgument(0);
            return ChatMessage.builder()
                    .messageId(messageIdSeq.incrementAndGet())
                    .conversationId(in.getConversationId())
                    .role(in.getRole())
                    .messageText(in.getMessageText())
                    .messageStatus(in.getMessageStatus())
                    .answerStatus(in.getAnswerStatus())
                    .errorCode(in.getErrorCode())
                    .modelName(in.getModelName())
                    .createdAt(in.getCreatedAt())
                    .build();
        });
        when(requestRagAnswerPort.requestAnswer(any())).thenReturn(RagAnswerResponse.builder()
                .answerText(null)
                .answerStatus(ChatAnswerStatus.ANSWERED)
                .sources(List.of())
                .build());
        lenient().when(loadChatSourcePort.findAllResultsByMessageId(anyLong())).thenReturn(List.of());

        AskChatCommand command = AskChatCommand.builder()
                .userId(userId).organizationId(100L).conversationId(conversationId)
                .question("범위 외 질문").documentScope(DocumentScope.ALL).build();

        AskChatResult result = chatService.execute(command);

        assertThat(result.getAssistantMessage().getAnswerStatus()).isEqualTo(ChatAnswerStatus.NO_RELEVANT_SOURCE);
        assertThat(result.getAssistantMessage().getMessageText()).contains("참조 가능한 문서를 찾지 못했습니다");
    }

    @Test
    @DisplayName("result_id 필요 안내는 실패 문구로 덮어쓰지 않고 그대로 반환한다")
    void validationAnswerPreserved() {
        long userId = 1L;
        long conversationId = 50L;
        ChatConversation conv = ChatConversation.builder()
                .conversationId(conversationId).userId(userId).title("t").build();
        when(loadChatConversationPort.findById(conversationId)).thenReturn(Optional.of(conv));

        AtomicLong messageIdSeq = new AtomicLong(8000L);
        when(saveChatMessagePort.save(any())).thenAnswer(invocation -> {
            ChatMessage in = invocation.getArgument(0);
            return ChatMessage.builder()
                    .messageId(messageIdSeq.incrementAndGet())
                    .conversationId(in.getConversationId())
                    .role(in.getRole())
                    .messageText(in.getMessageText())
                    .messageStatus(in.getMessageStatus())
                    .answerStatus(in.getAnswerStatus())
                    .errorCode(in.getErrorCode())
                    .modelName(in.getModelName())
                    .createdAt(in.getCreatedAt())
                    .build();
        });
        when(requestRagAnswerPort.requestAnswer(any())).thenReturn(RagAnswerResponse.builder()
                .answerText("검사 결과 설명을 위해 result_id가 필요합니다.")
                .answerStatus(ChatAnswerStatus.VALIDATION_FAILED)
                .sources(List.of())
                .build());
        lenient().when(loadChatSourcePort.findAllResultsByMessageId(anyLong())).thenReturn(List.of());

        AskChatCommand command = AskChatCommand.builder()
                .userId(userId).organizationId(100L).conversationId(conversationId)
                .question("이 결과 원인 알려줘").documentScope(DocumentScope.ALL).build();

        AskChatResult result = chatService.execute(command);

        assertThat(result.getAssistantMessage().getAnswerStatus()).isEqualTo(ChatAnswerStatus.VALIDATION_FAILED);
        assertThat(result.getAssistantMessage().getMessageStatus()).isEqualTo(ChatMessageStatus.SUCCESS);
        assertThat(result.getAssistantMessage().getMessageText()).isEqualTo("검사 결과 설명을 위해 result_id가 필요합니다.");
    }
}
