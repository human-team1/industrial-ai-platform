package com.example.factoryguard.common.validation;

import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileValidatorTest {

    private static byte[] validJpegBytes;
    private static byte[] validPngBytes;
    private FileValidator validator;

    @BeforeAll
    static void prepareValidImageBytes() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", jpegOut);
        validJpegBytes = jpegOut.toByteArray();
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        ImageIO.write(image, "png", pngOut);
        validPngBytes = pngOut.toByteArray();
    }

    @BeforeEach
    void setUp() {
        validator = new FileValidator(new FileValidationProperties());
    }

    @Test
    @DisplayName("No.6 허용 형식(jpg) 통과")
    void allowsJpegFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.jpg", "image/jpeg", validJpegBytes);
        assertThatCode(() -> validator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("No.6 허용 형식(png) 통과")
    void allowsPngFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.png", "image/png", validPngBytes);
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
                "file", "샘플.jpg", "image/jpeg", validJpegBytes);
        org.assertj.core.api.Assertions.assertThatCode(() -> validator.validate(file))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("INSP-001-07 손상 JPG (헤더 위반) - INVALID_FILE_CONTENT")
    void blocksCorruptJpegHeader() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "corrupt.jpg", "image/jpeg",
                new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_CONTENT);
    }

    @Test
    @DisplayName("INSP-001-07 손상 PNG (헤더 위반) - INVALID_FILE_CONTENT")
    void blocksCorruptPngHeader() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "corrupt.png", "image/png",
                new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_CONTENT);
    }

    @Test
    @DisplayName("INSP-001-07 JPG 매직 바이트만 있고 디코딩 불가 - INVALID_FILE_CONTENT")
    void blocksJpegWithMagicBytesButUndecodable() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_CONTENT);
    }

    @Test
    @DisplayName("INSP-001-07 WebP 정상 매직 바이트 통과 (디코딩 검증 제외)")
    void allowsWebpWithValidMagicBytes() {
        byte[] webpBytes = new byte[]{
                (byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46,
                0x00, 0x00, 0x00, 0x00,
                (byte) 0x57, (byte) 0x45, (byte) 0x42, (byte) 0x50
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample.webp", "image/webp", webpBytes);
        assertThatCode(() -> validator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("INSP-001-07 WebP 헤더 위반 - INVALID_FILE_CONTENT")
    void blocksCorruptWebpHeader() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "corrupt.webp", "image/webp",
                new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_CONTENT);
    }

    @Test
    @DisplayName("No.6 공백 포함 파일명(sample image.jpg) 차단 - INVALID_FILE_NAME")
    void blocksFilenameWithSpace() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "sample image.jpg", "image/jpeg", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_FILE_NAME);
    }
}
