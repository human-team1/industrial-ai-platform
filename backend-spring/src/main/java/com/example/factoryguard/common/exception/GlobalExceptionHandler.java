package com.example.factoryguard.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetailsResponse> handleBusinessException(
            BusinessException exception,
            WebRequest request
    ) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, exception.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ProblemDetailsResponse> handleValidationException(Exception exception, WebRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, exception.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailsResponse> handleException(Exception exception, WebRequest request) {
        log.error("Unhandled exception on {}: {}", request.getDescription(false), exception.getMessage(), exception);
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, errorCode.getDefaultMessage(), request.getDescription(false)));
    }

    private ProblemDetailsResponse toProblem(ErrorCode errorCode, String detail, String instance) {
        return new ProblemDetailsResponse(
                "about:blank",
                errorCode.getDefaultMessage(),
                errorCode.getStatus().value(),
                detail,
                instance,
                errorCode.getCode()
        );
    }
}
