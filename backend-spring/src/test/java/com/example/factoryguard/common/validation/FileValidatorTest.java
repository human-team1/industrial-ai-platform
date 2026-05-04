package com.example.factoryguard.common.validation;

import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileValidatorTest {

    private FileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileValidator(new FileValidationProperties());
    }

    @Test
    @DisplayName("No.6 허용 형식(jpg) 통과")
    void allowsJpegFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.jpg", "image/jpeg", new byte[]{1, 2, 3});
        assertThatCode(() -> validator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("No.6 허용 형식(png) 통과")
    void allowsPngFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.png", "image/png", new byte[]{1, 2, 3});
        assertThatCode(() -> validator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("No.6 sample.exe 차단 - INVALID_FILE_MIME")
    void blocksExeFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.exe", "application/octet-stream", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_MIME);
    }

    @Test
    @DisplayName("No.6 빈 파일 차단 - INVALID_FILE_EMPTY")
    void blocksEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.jpg", "image/jpeg", new byte[0]);
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_EMPTY);
    }

    @Test
    @DisplayName("No.6 경로 traversal 차단 - INVALID_FILE_NAME")
    void blocksPathTraversal() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../sample.jpg", "image/jpeg", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_NAME);
    }

    @Test
    @DisplayName("No.6 NUL 바이트 포함 파일명 차단 - INVALID_FILE_NAME")
    void blocksNulByteInName() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "evil\u0000name.jpg", "image/jpeg", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_NAME);
    }

    @Test
    @DisplayName("No.6 제어문자(개행) 포함 파일명 차단 - INVALID_FILE_NAME")
    void blocksControlChar() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "evil\nname.jpg", "image/jpeg", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_NAME);
    }

    @Test
    @DisplayName("No.6 Windows 절대경로(C:) 차단 - INVALID_FILE_NAME")
    void blocksWindowsAbsolutePath() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "C:evil.jpg", "image/jpeg", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_NAME);
    }

    @Test
    @DisplayName("No.6 Windows 예약어(CON.jpg) 차단 - INVALID_FILE_NAME")
    void blocksWindowsReservedName() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "CON.jpg", "image/jpeg", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_NAME);
    }

    @Test
    @DisplayName("No.6 비ASCII 특수문자(@#$) 차단 - INVALID_FILE_NAME")
    void blocksDisallowedSpecialChars() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "ev!l@.jpg", "image/jpeg", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_NAME);
    }

    @Test
    @DisplayName("No.6 한글 파일명(샘플.jpg) 통과")
    void allowsKoreanFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "샘플.jpg", "image/jpeg", new byte[]{1, 2, 3});
        org.assertj.core.api.Assertions.assertThatCode(() -> validator.validate(file))
                .doesNotThrowAnyException();
    }
}
