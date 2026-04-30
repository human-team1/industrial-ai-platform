package com.example.factoryguard.adapter.in.web.chat;

import com.example.factoryguard.adapter.in.web.chat.dto.AskChatRequest;
import com.example.factoryguard.adapter.in.web.chat.dto.CreateChatConversationRequest;
import com.example.factoryguard.adapter.in.web.chat.mapper.ChatWebMapper;
import com.example.factoryguard.application.dto.chat.AskChatResult;
import com.example.factoryguard.application.dto.chat.ChatConversationDetail;
import com.example.factoryguard.application.dto.chat.ChatConversationPageResult;
import com.example.factoryguard.application.dto.chat.ChatConversationResult;
import com.example.factoryguard.application.dto.chat.ChatMessageResult;
import com.example.factoryguard.application.dto.chat.ChatSourceResult;
import com.example.factoryguard.application.port.in.chat.AskChatUseCase;
import com.example.factoryguard.application.port.in.chat.CreateChatConversationUseCase;
import com.example.factoryguard.application.port.in.chat.DeleteChatConversationUseCase;
import com.example.factoryguard.application.port.in.chat.GetChatConversationUseCase;
import com.example.factoryguard.application.port.in.chat.ListChatConversationsUseCase;
import com.example.factoryguard.application.port.in.chat.ListChatMessagesUseCase;
import com.example.factoryguard.application.port.in.chat.ListChatSourcesUseCase;
import com.example.factoryguard.common.response.ApiResponse;
import com.example.factoryguard.config.security.AuthenticatedPrincipal;
import com.example.factoryguard.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

    private final AskChatUseCase askChatUseCase;
    private final CreateChatConversationUseCase createChatConversationUseCase;
    private final ListChatConversationsUseCase listChatConversationsUseCase;
    private final GetChatConversationUseCase getChatConversationUseCase;
    private final ListChatMessagesUseCase listChatMessagesUseCase;
    private final ListChatSourcesUseCase listChatSourcesUseCase;
    private final DeleteChatConversationUseCase deleteChatConversationUseCase;
    private final SecurityUtils securityUtils;
    private final ChatWebMapper chatWebMapper;

    @GetMapping("/chat-conversations")
    public ResponseEntity<ApiResponse<ChatConversationPageResult>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        ChatConversationPageResult result = listChatConversationsUseCase.execute(
                chatWebMapper.toListQuery(principal, page, size, keyword, from, to)
        );
        return ResponseEntity.ok(ApiResponse.success(result, "챗봇 대화 목록을 조회했습니다."));
    }

    @PostMapping("/chat-conversations")
    public ResponseEntity<ApiResponse<ChatConversationResult>> create(@RequestBody(required = false) CreateChatConversationRequest request) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        ChatConversationResult result = createChatConversationUseCase.create(
                chatWebMapper.toCreateConversationCommand(principal, request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "챗봇 대화가 생성되었습니다."));
    }

    @GetMapping("/chat-conversations/{conversationId}")
    public ResponseEntity<ApiResponse<ChatConversationDetail>> getDetail(@PathVariable Long conversationId) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        ChatConversationDetail result = getChatConversationUseCase.execute(principal.userId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success(result, "챗봇 대화 상세를 조회했습니다."));
    }

    @DeleteMapping("/chat-conversations/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long conversationId) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        deleteChatConversationUseCase.delete(principal.userId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success(null, "챗봇 대화가 삭제되었습니다."));
    }

    @GetMapping("/chat-conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResult>>> listMessages(@PathVariable Long conversationId) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        List<ChatMessageResult> result = listChatMessagesUseCase.listMessages(principal.userId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success(result, "챗봇 메시지 목록을 조회했습니다."));
    }

    @PostMapping("/chat-conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<AskChatResult>> ask(
            @PathVariable Long conversationId,
            @RequestBody AskChatRequest request
    ) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        AskChatResult result = askChatUseCase.execute(
                chatWebMapper.toAskChatCommand(principal, conversationId, request)
        );
        return ResponseEntity.ok(ApiResponse.success(result, "챗봇 답변이 생성되었습니다."));
    }

    @GetMapping("/chat-messages/{messageId}/sources")
    public ResponseEntity<ApiResponse<List<ChatSourceResult>>> listSources(@PathVariable Long messageId) {
        AuthenticatedPrincipal principal = securityUtils.getCurrentPrincipal();
        List<ChatSourceResult> result = listChatSourcesUseCase.listSources(principal.userId(), messageId);
        return ResponseEntity.ok(ApiResponse.success(result, "챗봇 답변 출처를 조회했습니다."));
    }

}
