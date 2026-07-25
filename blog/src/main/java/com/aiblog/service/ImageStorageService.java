package com.aiblog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);

    @Value("${app.image.storage-dir:./data/images}")
    private String storageDir;

    @Value("${app.image.url-prefix:/images}")
    private String urlPrefix;

    private Path storagePath;

    /**
     * Allowed image content types for multipart uploads.
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp", "image/svg+xml"
    );

    @PostConstruct
    public void init() throws IOException {
        storagePath = Paths.get(storageDir).toAbsolutePath().normalize();
        Files.createDirectories(storagePath);
        log.info("Image storage directory: {}", storagePath);
    }

    /**
     * Detects whether the input is a Base64-encoded image.
     * Supports both raw Base64 and data URI format (data:image/png;base64,...).
     */
    public boolean isBase64Image(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        if (input.startsWith("data:image/")) {
            return true;
        }
        // Not a URL -> treat as raw Base64
        return !input.startsWith("http://") && !input.startsWith("https://") && !input.startsWith("/");
    }

    /**
     * Decodes a Base64-encoded image and saves it to local storage.
     * Returns the URL path that can be used to access the image.
     */
    public String saveBase64Image(String base64Input) throws IOException {
        String base64Data;
        String extension;

        if (base64Input.startsWith("data:image/")) {
            // Parse data URI: data:image/png;base64,iVBOR...
            int semicolonIdx = base64Input.indexOf(';');
            int commaIdx = base64Input.indexOf(',');
            extension = base64Input.substring("data:image/".length(), semicolonIdx);
            base64Data = base64Input.substring(commaIdx + 1);
        } else {
            // Raw Base64, default to png
            base64Data = base64Input;
            extension = "png";
        }

        // Normalize extension
        extension = switch (extension.toLowerCase()) {
            case "jpeg" -> "jpg";
            case "svg+xml" -> "svg";
            default -> extension.toLowerCase();
        };

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        String fileName = UUID.randomUUID() + "." + extension;
        Path filePath = storagePath.resolve(fileName);
        Files.write(filePath, imageBytes);

        log.info("Saved image: {} ({} bytes)", fileName, imageBytes.length);
        return urlPrefix + "/" + fileName;
    }

    /**
     * Saves an uploaded image to local storage from raw bytes plus metadata.
     * This entry point is transport-agnostic: callers (e.g. controllers wrapping
     * {@code MultipartFile}, batch importers, scheduled jobs) extract bytes and
     * metadata themselves and hand them in.
     *
     * Validates that the byte array is non-empty and the content type is an
     * allowed image type. Returns the URL path that can be used to access the
     * stored image.
     *
     * @param data             raw image bytes (must be non-null and non-empty)
     * @param contentType      MIME type, e.g. {@code image/png} (must be in the allow-list)
     * @param originalFilename original filename, used to preserve the extension; may be {@code null}
     * @return the public URL path of the stored image (e.g. {@code /images/<uuid>.png})
     * @throws IllegalArgumentException if {@code data} is null/empty or {@code contentType} is not allowed
     * @throws IOException              if writing the file to disk fails
     */
    public String saveImageFile(byte[] data, String contentType, String originalFilename) throws IOException {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Image file is required and must not be empty");
        }

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Unsupported image content type: " + contentType
                    + ". Allowed: " + ALLOWED_CONTENT_TYPES);
        }

        String extension = resolveExtension(contentType, originalFilename);
        String fileName = UUID.randomUUID() + "." + extension;
        Path filePath = storagePath.resolve(fileName);
        Files.write(filePath, data);

        log.info("Saved uploaded image: {} ({} bytes, type={})", fileName, data.length, contentType);
        return urlPrefix + "/" + fileName;
    }

    private String resolveExtension(String contentType, String originalFilename) {
        // Prefer original filename extension when available
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0 && dot < originalFilename.length() - 1) {
                String ext = originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
                if (ext.matches("[a-z0-9]{2,5}")) {
                    return "jpeg".equals(ext) ? "jpg" : ext;
                }
            }
        }
        // Fall back to content type
        String subtype = contentType.substring(contentType.indexOf('/') + 1).toLowerCase(Locale.ROOT);
        return switch (subtype) {
            case "jpeg" -> "jpg";
            case "svg+xml" -> "svg";
            default -> subtype;
        };
    }
}
