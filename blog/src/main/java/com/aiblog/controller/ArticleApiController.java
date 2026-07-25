package com.aiblog.controller;

import com.aiblog.dto.ArticleCreateRequest;
import com.aiblog.dto.ArticleImagePatchRequest;
import com.aiblog.dto.ArticleResponse;
import com.aiblog.dto.ArticleUpdateRequest;
import com.aiblog.service.ArticleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        log.info("API: GET /api/articles - tag={}, category={}, page={}, size={}",
                tag, category, pageable.getPageNumber(), pageable.getPageSize());
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
     * Patches image references on an existing article.
     *
     * <p>Supports two independent operations in a single call:
     * <ul>
     *   <li>Replace the article cover image by providing {@code coverImageUrl}.</li>
     *   <li>Replace placeholder strings in the Markdown body by providing
     *       {@code contentReplacements} (a map of placeholder → replacement value).</li>
     * </ul>
     * Both fields are optional, but at least one must be non-null/non-empty.
     *
     * <p>Auth: requires {@code X-API-Key} header (enforced by ApiKeyInterceptor).
     */
    @PatchMapping("/{id}/images")
    public ResponseEntity<ArticleResponse> patchImages(@PathVariable Long id,
                                                        @RequestBody ArticleImagePatchRequest request) {
        log.info("API: PATCH /api/articles/{}/images", id);

        boolean hasCover = request.getCoverImageUrl() != null && !request.getCoverImageUrl().isBlank();
        boolean hasReplacements = request.getContentReplacements() != null
                && !request.getContentReplacements().isEmpty();

        if (!hasCover && !hasReplacements) {
            throw new IllegalArgumentException(
                    "At least one of 'coverImageUrl' or 'contentReplacements' must be provided");
        }

        ArticleResponse response = articleService.patchImages(id, request);
        return ResponseEntity.ok(response);
    }
}
