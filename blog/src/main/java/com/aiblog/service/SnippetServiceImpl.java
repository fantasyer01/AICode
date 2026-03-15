package com.aiblog.service;

import com.aiblog.audit.Auditable;
import com.aiblog.dto.SnippetCreateRequest;
import com.aiblog.dto.SnippetResponse;
import com.aiblog.exception.ResourceNotFoundException;
import com.aiblog.model.Snippet;
import com.aiblog.model.SnippetStatus;
import com.aiblog.repository.SnippetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@Transactional
public class SnippetServiceImpl implements SnippetService {

    private static final Logger log = LoggerFactory.getLogger(SnippetServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final SnippetRepository snippetRepository;
    private final DeepSeekService deepSeekService;
    private final MarkdownService markdownService;

    public SnippetServiceImpl(SnippetRepository snippetRepository,
                              DeepSeekService deepSeekService,
                              MarkdownService markdownService) {
        this.snippetRepository = snippetRepository;
        this.deepSeekService = deepSeekService;
        this.markdownService = markdownService;
    }

    @Override
    @Auditable(operation = "INSERT", entityType = "Snippet")
    public SnippetResponse create(SnippetCreateRequest request) {
        Snippet snippet = new Snippet();
        snippet.setRawContent(request.getRawContent());
        snippet.setAuthor(request.getAuthor());
        snippet.setStatus(SnippetStatus.PENDING);

        processWithLlm(snippet);

        Snippet saved = snippetRepository.save(snippet);
        log.info("Created snippet id={}, status={}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SnippetResponse getById(Long id) {
        Snippet snippet = getEntityById(id);
        return toResponse(snippet);
    }

    @Override
    @Auditable(operation = "UPDATE", entityType = "Snippet")
    public SnippetResponse update(Long id, SnippetCreateRequest request) {
        Snippet snippet = getEntityById(id);
        snippet.setRawContent(request.getRawContent());
        if (request.getAuthor() != null) {
            snippet.setAuthor(request.getAuthor());
        }
        snippet.setStatus(SnippetStatus.PENDING);

        processWithLlm(snippet);

        Snippet saved = snippetRepository.save(snippet);
        log.info("Updated snippet id={}, status={}", saved.getId(), saved.getStatus());
        return toResponse(saved);
    }

    @Override
    @Auditable(operation = "DELETE", entityType = "Snippet")
    public void delete(Long id) {
        Snippet snippet = getEntityById(id);
        snippetRepository.delete(snippet);
        log.info("Deleted snippet id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SnippetResponse> list(Pageable pageable) {
        return snippetRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Snippet getEntityById(Long id) {
        return snippetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snippet", id));
    }

    @Override
    @Transactional(readOnly = true)
    public LinkedHashMap<String, List<SnippetResponse>> listGroupedByDate(Pageable pageable) {
        Page<Snippet> page = snippetRepository.findAllByOrderByCreatedAtDesc(pageable);
        LinkedHashMap<String, List<SnippetResponse>> grouped = new LinkedHashMap<>();
        for (Snippet snippet : page.getContent()) {
            String dateKey = snippet.getCreatedAt().format(DATE_FORMATTER);
            grouped.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(toResponse(snippet));
        }
        return grouped;
    }

    private void processWithLlm(Snippet snippet) {
        try {
            DeepSeekService.ProcessingResult result = deepSeekService.processSnippet(snippet.getRawContent());
            snippet.setProcessedTitle(result.title());
            snippet.setProcessedContent(result.content());
            snippet.setTags(result.tags() != null ? result.tags() : new ArrayList<>());
            snippet.setStatus(SnippetStatus.PROCESSED);
        } catch (Exception e) {
            log.warn("LLM processing failed for snippet: {}", e.getMessage());
            snippet.setStatus(SnippetStatus.FAILED);
        }
    }

    private SnippetResponse toResponse(Snippet snippet) {
        String displayContent;
        if (snippet.getStatus() == SnippetStatus.PROCESSED && snippet.getProcessedContent() != null) {
            displayContent = snippet.getProcessedContent();
        } else {
            displayContent = snippet.getRawContent();
        }
        String displayContentHtml = markdownService.renderToHtml(displayContent);
        return SnippetResponse.fromEntity(snippet, displayContentHtml);
    }
}
