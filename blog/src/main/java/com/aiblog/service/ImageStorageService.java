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
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);

    @Value("${app.image.storage-dir:./data/images}")
    private String storageDir;

    @Value("${app.image.url-prefix:/images}")
    private String urlPrefix;

    private Path storagePath;

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
}
