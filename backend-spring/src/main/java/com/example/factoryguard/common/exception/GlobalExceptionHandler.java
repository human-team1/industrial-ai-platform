package com.example.factoryguard.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

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
                .body(toProblem(errorCode, exception.getMessage(), request.getDescription(false), null));
    }

    /**
     * @Valid 기반 필드 검증 실패 — 422 Unprocessable Content.
     * RFC 9457 Problem Details에 errors[]로 어느 필드가 왜 실패했는지 포함.
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ProblemDetailsResponse> handleValidationException(BindException exception, WebRequest request) {
        List<ValidationFieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toValidationFieldError)
                .collect(Collectors.toList());
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, errorCode.getDefaultMessage(), request.getDescription(false), errors));
    }

    /**
     * @PathVariable, @RequestParam 등에 걸린 javax.validation 제약 위반 — 422.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetailsResponse> handleConstraintViolationException(
            ConstraintViolationException exception, WebRequest request) {
        List<ValidationFieldError> errors = exception.getConstraintViolations().stream()
                .map(this::toValidationFieldError)
                .collect(Collectors.toList());
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, errorCode.getDefaultMessage(), request.getDescription(false), errors));
    }

    /**
     * JSON 문법 오류, 본문 파싱 실패 — 400 Bad Request 유지.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetailsResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception, WebRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, "요청 본문을 해석할 수 없습니다.", request.getDescription(false), null));
    }

    /**
     * 필수 query parameter 누락 — 400 Bad Request 유지.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetailsResponse> handleMissingParam(
            MissingServletRequestParameterException exception, WebRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, exception.getMessage(), request.getDescription(false), null));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ProblemDetailsResponse> handleMissingPart(
            MissingServletRequestPartException exception, WebRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, exception.getMessage(), request.getDescription(false), null));
    }

    /**
     * path/query parameter type mismatch — 400 Bad Request 유지.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetailsResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, WebRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, exception.getMessage(), request.getDescription(false), null));
    }

    /**
     * 업로드 파일이 spring.servlet.multipart.max-file-size를 초과 — 413 Payload Too Large.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetailsResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException exception, WebRequest request) {
        ErrorCode errorCode = ErrorCode.UPLOAD_SIZE_EXCEEDED;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, errorCode.getDefaultMessage(), request.getDescription(false), null));
    }

    /**
     * multipart/form-data 파싱 실패 (잘못된 boundary, 손상된 본문 등) — 415 Unsupported Media Type.
     * MaxUploadSizeExceededException은 위에서 먼저 매칭되므로 일반 Multipart 오류만 잡는다.
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ProblemDetailsResponse> handleMultipartException(
            MultipartException exception, WebRequest request) {
        ErrorCode errorCode = ErrorCode.MULTIPART_PARSE_FAILED;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, errorCode.getDefaultMessage(), request.getDescription(false), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailsResponse> handleException(Exception exception, WebRequest request) {
        log.error("Unhandled exception on {}: {}", request.getDescription(false), exception.getMessage(), exception);
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(toProblem(errorCode, errorCode.getDefaultMessage(), request.getDescription(false), null));
    }

    private ValidationFieldError toValidationFieldError(FieldError fieldError) {
        return new ValidationFieldError(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ValidationFieldError toValidationFieldError(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "";
        String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
        return new ValidationFieldError(field, violation.getMessage());
    }

    private ProblemDetailsResponse toProblem(ErrorCode errorCode, String detail, String instance,
                                             List<ValidationFieldError> errors) {
        return new ProblemDetailsResponse(
                "about:blank",
                errorCode.getDefaultMessage(),
                errorCode.getStatus().value(),
                detail,
                instance,
                errorCode.getCode(),
                errors
        );
    }
}
