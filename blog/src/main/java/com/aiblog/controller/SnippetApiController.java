package com.aiblog.controller;

import com.aiblog.dto.SnippetCreateRequest;
import com.aiblog.dto.SnippetResponse;
import com.aiblog.service.SnippetService;
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
@RequestMapping("/api/snippets")
public class SnippetApiController {

    private static final Logger log = LoggerFactory.getLogger(SnippetApiController.class);

    private final SnippetService snippetService;

    public SnippetApiController(SnippetService snippetService) {
        this.snippetService = snippetService;
    }

    @PostMapping
    public ResponseEntity<SnippetResponse> create(@Valid @RequestBody SnippetCreateRequest request) {
        log.info("API: POST /api/snippets - rawContent length={}", request.getRawContent().length());
        SnippetResponse response = snippetService.create(request);
        URI location = URI.create("/api/snippets/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SnippetResponse> getById(@PathVariable Long id) {
        log.info("API: GET /api/snippets/{}", id);
        SnippetResponse response = snippetService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SnippetResponse>> list(@PageableDefault(size = 10) Pageable pageable) {
        log.info("API: GET /api/snippets - page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<SnippetResponse> page = snippetService.list(pageable);
        return ResponseEntity.ok(page);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("API: DELETE /api/snippets/{}", id);
        snippetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
