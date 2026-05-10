package com.example.factoryguard.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("No.50 BusinessException - Problem Details 형식(status, title, detail, errorCode) 반환")
    void businessExceptionReturnsProblemDetails() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/v1/inspections/upload");

        BusinessException exception = new BusinessException(ErrorCode.INVALID_FILE_MIME, "허용되지 않는 MIME 타입");

        ResponseEntity<ProblemDetailsResponse> response = handler.handleBusinessException(exception, req);

        assertThat(response.getStatusCodeValue()).isEqualTo(422);
        ProblemDetailsResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(422);
        assertThat(body.getTitle()).isEqualTo(ErrorCode.INVALID_FILE_MIME.getDefaultMessage());
        assertThat(body.getDetail()).isEqualTo("허용되지 않는 MIME 타입");
        assertThat(body.getCode()).isEqualTo("FILE-402");
        assertThat(body.getInstance()).isEqualTo("uri=/api/v1/inspections/upload");
        assertThat(body.getType()).isEqualTo("about:blank");
    }

    @Test
    @DisplayName("No.50 BindException - VALIDATION_FAILED + errors[] 필드 검증 실패 정보 포함")
    void bindExceptionReturnsValidationErrors() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/v1/inspections/upload");

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "uploadRequest");
        bindingResult.addError(new FieldError(
                "uploadRequest", "roiX", null, false,
                new String[]{"range"}, null, "0과 1 사이여야 합니다."));
        BindException bindException = new BindException(bindingResult);

        ResponseEntity<ProblemDetailsResponse> response = handler.handleValidationException(bindException, req);

        assertThat(response.getStatusCodeValue()).isEqualTo(422);
        ProblemDetailsResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("VALIDATION-422");
        assertThat(body.getErrors()).hasSize(1);
        assertThat(body.getErrors().get(0).getField()).isEqualTo("roiX");
        assertThat(body.getErrors().get(0).getReason()).isEqualTo("0과 1 사이여야 합니다.");
    }

    @Test
    @DisplayName("No.50 ConstraintViolation - VALIDATION_FAILED + 필드 경로에서 마지막 segment 추출")
    void constraintViolationExtractsLeafFieldName() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/v1/inspections");

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        javax.validation.Path path = mock(javax.validation.Path.class);
        when(path.toString()).thenReturn("submit.command.roiX");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be between 0 and 1");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ProblemDetailsResponse> response = handler.handleConstraintViolationException(ex, req);

        assertThat(response.getStatusCodeValue()).isEqualTo(422);
        ProblemDetailsResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrors()).hasSize(1);
        assertThat(body.getErrors().get(0).getField()).isEqualTo("roiX");
        assertThat(body.getErrors().get(0).getReason()).isEqualTo("must be between 0 and 1");
    }

    @Test
    @DisplayName("No.50 MaxUploadSizeExceededException - 413 + UPLOAD-413 errorCode")
    void uploadSizeExceededReturnsPayloadTooLarge() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/v1/inspections/upload");
        org.springframework.web.multipart.MaxUploadSizeExceededException ex =
                new org.springframework.web.multipart.MaxUploadSizeExceededException(50L * 1024 * 1024);

        ResponseEntity<ProblemDetailsResponse> response = handler.handleMaxUploadSizeExceeded(ex, req);

        assertThat(response.getStatusCodeValue()).isEqualTo(413);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("UPLOAD-413");
    }

    @Test
    @DisplayName("No.50 MultipartException - 415 + UPLOAD-415 errorCode")
    void multipartParseFailureReturnsUnsupportedMediaType() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/v1/documents");
        org.springframework.web.multipart.MultipartException ex =
                new org.springframework.web.multipart.MultipartException("malformed multipart");

        ResponseEntity<ProblemDetailsResponse> response = handler.handleMultipartException(ex, req);

        assertThat(response.getStatusCodeValue()).isEqualTo(415);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("UPLOAD-415");
    }

    @Test
    @DisplayName("No.50 일반 Exception - INTERNAL_ERROR(500) + about:blank type 응답")
    void unknownExceptionFallsBackToInternalError() {
        WebRequest req = mock(WebRequest.class);
        when(req.getDescription(false)).thenReturn("uri=/api/v1/anything");

        ResponseEntity<ProblemDetailsResponse> response =
                handler.handleException(new RuntimeException("boom"), req);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("COMMON-500");
        assertThat(response.getBody().getType()).isEqualTo("about:blank");
    }
}
