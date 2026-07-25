package com.aiblog.controller;

import com.aiblog.dto.ImageUploadResponse;
import com.aiblog.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageApiController {

    private static final Logger log = LoggerFactory.getLogger(ImageApiController.class);

    private final ImageStorageService imageStorageService;

    public ImageApiController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    /**
     * Uploads an image file and returns its publicly accessible URL.
     *
     * <p>This is the single entry point for all image uploads (article cover,
     * inline body images, etc.). Callers obtain a URL and reference it wherever
     * needed — e.g. as {@code coverImageUrl} in {@code POST /api/articles}, or
     * embedded in Markdown content as {@code ![alt](url)}.
     *
     * <p>Auth: requires {@code X-API-Key} header (enforced by ApiKeyInterceptor).
     *
     * @param file the image file (PNG, JPEG, GIF, WebP, SVG; max 5 MB)
     * @return {@code 200 OK} with {@code {"url": "/images/<uuid>.<ext>"}}
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        log.info("API: POST /api/images/upload - file='{}', size={}, type={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required and must not be empty");
        }

        try {
            String url = imageStorageService.saveImageFile(
                    file.getBytes(), file.getContentType(), file.getOriginalFilename());
            log.info("Image uploaded successfully: {}", url);
            return ResponseEntity.ok(new ImageUploadResponse(url));
        } catch (IOException e) {
            log.error("Failed to save uploaded image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save uploaded image", e);
        }
    }
}
