package com.example.factoryguard.common.validation;

import com.example.factoryguard.common.exception.BusinessException;
import com.example.factoryguard.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class FileValidator {

    private static final int MAX_FILENAME_LENGTH = 255;
    private static final Pattern FILENAME_WHITELIST = Pattern.compile("^[A-Za-z0-9._\\-()\\uAC00-\\uD7A3]+$");
    private static final Set<String> WINDOWS_RESERVED_NAMES = Set.of(
            "con", "prn", "aux", "nul",
            "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
            "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
    );
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> DECODABLE_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final FileValidationProperties properties;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EMPTY);
        }
        if (file.getSize() > properties.getMaxSize().toBytes()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_SIZE);
        }
        String mime = file.getContentType();
        if (mime == null || !properties.getAllowedMimeTypes().contains(mime.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.INVALID_FILE_MIME);
        }
        String original = file.getOriginalFilename();
        validateFilename(original);
        String ext = extractExtension(original);
        if (ext == null || !properties.getAllowedExtensions().contains(ext)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
        }
        if (IMAGE_EXTENSIONS.contains(ext)) {
            validateImageMagicBytes(file, ext);
            if (DECODABLE_IMAGE_EXTENSIONS.contains(ext)) {
                validateImageDecodable(file);
            }
        }
    }

    private void validateImageMagicBytes(MultipartFile file, String ext) {
        byte[] header;
        try (InputStream input = file.getInputStream()) {
            header = input.readNBytes(12);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INVALID_FILE_CONTENT);
        }
        boolean valid = switch (ext) {
            case "jpg", "jpeg" -> header.length >= 3
                    && header[0] == (byte) 0xFF
                    && header[1] == (byte) 0xD8
                    && header[2] == (byte) 0xFF;
            case "png" -> header.length >= 8
                    && header[0] == (byte) 0x89
                    && header[1] == (byte) 0x50
                    && header[2] == (byte) 0x4E
                    && header[3] == (byte) 0x47
                    && header[4] == (byte) 0x0D
                    && header[5] == (byte) 0x0A
                    && header[6] == (byte) 0x1A
                    && header[7] == (byte) 0x0A;
            case "webp" -> header.length >= 12
                    && header[0] == (byte) 0x52
                    && header[1] == (byte) 0x49
                    && header[2] == (byte) 0x46
                    && header[3] == (byte) 0x46
                    && header[8] == (byte) 0x57
                    && header[9] == (byte) 0x45
                    && header[10] == (byte) 0x42
                    && header[11] == (byte) 0x50;
            default -> true;
        };
        if (!valid) {
            throw new BusinessException(ErrorCode.INVALID_FILE_CONTENT);
        }
    }

    private void validateImageDecodable(MultipartFile file) {
        BufferedImage image;
        try (InputStream input = file.getInputStream()) {
            image = ImageIO.read(input);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INVALID_FILE_CONTENT);
        }
        if (image == null) {
            throw new BusinessException(ErrorCode.INVALID_FILE_CONTENT);
        }
    }

    private void validateFilename(String original) {
        if (original == null || original.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }
        if (original.length() > MAX_FILENAME_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }
        if (containsForbiddenWhitespaceOrControl(original)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }
        if (original.contains("..") || original.contains("/") || original.contains("\\")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }
        if (original.length() >= 2 && original.charAt(1) == ':') {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }
        if (!FILENAME_WHITELIST.matcher(original).matches()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }
        String stem = stripExtension(original).toLowerCase(Locale.ROOT);
        if (WINDOWS_RESERVED_NAMES.contains(stem)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
        }
    }

    private boolean containsForbiddenWhitespaceOrControl(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                return true;
            }
            if (c < 0x20 || c == 0x7F) {
                return true;
            }
        }
        return false;
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0) {
            return filename;
        }
        return filename.substring(0, dot);
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
