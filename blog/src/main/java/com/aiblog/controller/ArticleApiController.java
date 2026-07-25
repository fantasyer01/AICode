package com.aiblog.controller;

import com.aiblog.dto.ArticleCoverImageUpdateRequest;
import com.aiblog.dto.ArticleCreateRequest;
import com.aiblog.dto.ArticleResponse;
import com.aiblog.dto.ArticleUpdateRequest;
import com.aiblog.service.ArticleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/articles")
public class ArticleApiController {

    private static final Logger log = LoggerFactory.getLogger(ArticleApiController.class);

    private final ArticleService articleService;

    public ArticleApiController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> create(@Valid @RequestBody ArticleCreateRequest request) {
        log.info("API: POST /api/articles - title='{}'", request.getTitle());
        ArticleResponse response = articleService.create(request);
        URI location = URI.create("/api/articles/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getById(@PathVariable Long id) {
        log.info("API: GET /api/articles/{}", id);
        ArticleResponse response = articleService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody ArticleUpdateRequest request) {
        log.info("API: PUT /api/articles/{}", id);
        ArticleResponse response = articleService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ArticleResponse>> list(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("API: GET /api/articles - tag={}, category={}, page={}, size={}", tag, category, pageable.getPageNumber(), pageable.getPageSize());
        Page<ArticleResponse> page;
        if (category != null && !category.isBlank()) {
            page = articleService.listByCategory(category.trim(), pageable);
        } else if (tag != null && !tag.isBlank()) {
            page = articleService.listByTag(tag.trim(), pageable);
        } else {
            page = articleService.list(pageable);
        }
        return ResponseEntity.ok(page);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("API: DELETE /api/articles/{}", id);
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Replace (or set) an article's cover image via multipart upload.
     * Used both when the article never had a cover image, and when the user
     * wants to change the existing cover.
     *
     * <p>The Controller is the only layer aware of the {@link MultipartFile}
     * framework type: it unwraps the bytes and metadata into a transport-agnostic
     * {@link ArticleCoverImageUpdateRequest} DTO before invoking the service,
     * keeping the service layer free of any web/HTTP dependency (consistent with
     * {@code create} / {@code update} which also take pure DTOs).
     *
     * <p>Auth: requires {@code X-API-Key} header (enforced by ApiKeyInterceptor).
     */
    @PutMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArticleResponse> updateCoverImage(@PathVariable Long id,
                                                            @RequestParam("file") MultipartFile file) {
        log.info("API: PUT /api/articles/{}/cover - file='{}', size={}, type={}",
                id, file.getOriginalFilename(), file.getSize(), file.getContentType());

        ArticleCoverImageUpdateRequest request = toCoverImageRequest(file);
        ArticleResponse response = articleService.updateCoverImage(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Adapts a {@link MultipartFile} into the transport-agnostic DTO consumed by the service layer.
     * Empty / null files are surfaced as {@link IllegalArgumentException} which the global
     * exception handler converts to {@code 400 Bad Request}.
     */
    private ArticleCoverImageUpdateRequest toCoverImageRequest(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required and must not be empty");
        }
        ArticleCoverImageUpdateRequest req = new ArticleCoverImageUpdateRequest();
        try {
            req.setData(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file bytes", e);
        }
        req.setContentType(file.getContentType());
        req.setOriginalFilename(file.getOriginalFilename());
        return req;
    }
}
