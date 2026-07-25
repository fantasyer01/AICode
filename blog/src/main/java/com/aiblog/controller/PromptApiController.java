package com.aiblog.controller;

import com.aiblog.dto.PromptCreateRequest;
import com.aiblog.dto.PromptResponse;
import com.aiblog.dto.PromptUpdateRequest;
import com.aiblog.service.PromptService;
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
@RequestMapping("/api/prompts")
public class PromptApiController {

    private static final Logger log = LoggerFactory.getLogger(PromptApiController.class);

    private final PromptService promptService;

    public PromptApiController(PromptService promptService) {
        this.promptService = promptService;
    }

    @PostMapping
    public ResponseEntity<PromptResponse> create(@Valid @RequestBody PromptCreateRequest request) {
        log.info("API: POST /api/prompts - title='{}'", request.getTitle());
        PromptResponse response = promptService.create(request);
        URI location = URI.create("/api/prompts/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromptResponse> getById(@PathVariable Long id) {
        log.info("API: GET /api/prompts/{}", id);
        PromptResponse response = promptService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<PromptResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("API: GET /api/prompts - keyword={}, source={}, model={}, tag={}", keyword, source, model, tag);
        Page<PromptResponse> page;
        boolean hasFilter = (keyword != null && !keyword.isBlank())
                || (source != null && !source.isBlank())
                || (model != null && !model.isBlank())
                || (tag != null && !tag.isBlank());
        if (hasFilter) {
            page = promptService.search(keyword, source, model, tag, pageable);
        } else {
            page = promptService.listPublished(pageable);
        }
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromptResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody PromptUpdateRequest request) {
        log.info("API: PUT /api/prompts/{}", id);
        PromptResponse response = promptService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("API: DELETE /api/prompts/{}", id);
        promptService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
