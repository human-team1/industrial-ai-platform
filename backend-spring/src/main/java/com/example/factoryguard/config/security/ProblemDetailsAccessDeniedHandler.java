package com.example.factoryguard.config.security;

import com.example.factoryguard.application.dto.operation.RecordOperationLogCommand;
import com.example.factoryguard.application.port.in.operation.RecordOperationLogUseCase;
import com.example.factoryguard.common.exception.ErrorCode;
import com.example.factoryguard.common.exception.ProblemDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ProblemDetailsAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final RecordOperationLogUseCase recordOperationLogUseCase;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        ProblemDetailsResponse body = new ProblemDetailsResponse(
                "about:blank",
                errorCode.getDefaultMessage(),
                errorCode.getStatus().value(),
                errorCode.getDefaultMessage(),
                request.getRequestURI(),
                errorCode.getCode()
        );

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        recordAdminAccessDenied(request);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private void recordAdminAccessDenied(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/v1/admin/")) {
            return;
        }
        recordOperationLogUseCase.recordOperationLog(RecordOperationLogCommand.builder()
                .eventType("ADMIN_API_ACCESS_DENIED")
                .eventStatus("FAILED")
                .logLevel("WARN")
                .sourceComponent("SPRING_API")
                .detailMessage("관리자 API 권한 없는 접근이 차단되었습니다.")
                .relatedPath(path)
                .build());
    }
}
