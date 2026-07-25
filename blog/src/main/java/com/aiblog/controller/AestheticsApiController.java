package com.aiblog.controller;

import com.aiblog.dto.AestheticsArticleCreateRequest;
import com.aiblog.dto.AestheticsArticleResponse;
import com.aiblog.dto.AestheticsArticleUpdateRequest;
import com.aiblog.service.AestheticsArticleService;
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
@RequestMapping("/api/aesthetics")
public class AestheticsApiController {

    private static final Logger log = LoggerFactory.getLogger(AestheticsApiController.class);

    private final AestheticsArticleService articleService;

    public AestheticsApiController(AestheticsArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<AestheticsArticleResponse> create(@Valid @RequestBody AestheticsArticleCreateRequest request) {
        log.info("API: POST /api/aesthetics - title='{}'", request.getTitle());
        AestheticsArticleResponse response = articleService.create(request);
        URI location = URI.create("/api/aesthetics/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AestheticsArticleResponse> getById(@PathVariable Long id) {
        log.info("API: GET /api/aesthetics/{}", id);
        AestheticsArticleResponse response = articleService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<AestheticsArticleResponse>> list(
            @RequestParam(required = false) Long sensory,
            @RequestParam(required = false) Long domain,
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("API: GET /api/aesthetics - sensory={}, domain={}, tag={}", sensory, domain, tag);
        Page<AestheticsArticleResponse> page;
        if (sensory != null && domain != null) {
            page = articleService.listPublishedByDimensions(sensory, domain, pageable);
        } else if (sensory != null) {
            page = articleService.listPublishedBySensory(sensory, pageable);
        } else if (domain != null) {
            page = articleService.listPublishedByDomain(domain, pageable);
        } else if (tag != null && !tag.isBlank()) {
            page = articleService.listPublishedByTag(tag.trim(), pageable);
        } else {
            page = articleService.listPublished(pageable);
        }
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AestheticsArticleResponse> update(@PathVariable Long id,
                                                            @Valid @RequestBody AestheticsArticleUpdateRequest request) {
        log.info("API: PUT /api/aesthetics/{}", id);
        AestheticsArticleResponse response = articleService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("API: DELETE /api/aesthetics/{}", id);
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
