package com.aiblog.service;

import com.aiblog.audit.Auditable;
import com.aiblog.dto.PromptCreateRequest;
import com.aiblog.dto.PromptResponse;
import com.aiblog.dto.PromptUpdateRequest;
import com.aiblog.exception.ResourceNotFoundException;
import com.aiblog.model.Prompt;
import com.aiblog.model.PromptSource;
import com.aiblog.repository.PromptRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PromptServiceImpl implements PromptService {

    private static final Logger log = LoggerFactory.getLogger(PromptServiceImpl.class);

    private final PromptRepository promptRepository;
    private final MarkdownService markdownService;

    public PromptServiceImpl(PromptRepository promptRepository, MarkdownService markdownService) {
        this.promptRepository = promptRepository;
        this.markdownService = markdownService;
    }

    @Override
    @Auditable(operation = "INSERT", entityType = "Prompt")
    public PromptResponse create(PromptCreateRequest request) {
        Prompt prompt = new Prompt();
        prompt.setTitle(request.getTitle());
        prompt.setContent(request.getContent());
        prompt.setDescription(request.getDescription());
        prompt.setNotes(request.getNotes());
        prompt.setSource(request.getSource());
        prompt.setModel(request.getModel());
        prompt.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        prompt.setPublished(request.getPublished() != null ? request.getPublished() : true);

        Prompt saved = promptRepository.save(prompt);
        log.info("Created prompt id={}, title='{}'", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PromptResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Override
    @Auditable(operation = "UPDATE", entityType = "Prompt")
    public PromptResponse update(Long id, PromptUpdateRequest request) {
        Prompt prompt = getEntityById(id);

        if (request.getTitle() != null) {
            prompt.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            prompt.setContent(request.getContent());
        }
        if (request.getDescription() != null) {
            prompt.setDescription(request.getDescription());
        }
        if (request.getNotes() != null) {
            prompt.setNotes(request.getNotes());
        }
        if (request.getSource() != null) {
            prompt.setSource(request.getSource());
        }
        if (request.getModel() != null) {
            prompt.setModel(request.getModel());
        }
        if (request.getTags() != null) {
            prompt.setTags(request.getTags());
        }
        if (request.getPublished() != null) {
            prompt.setPublished(request.getPublished());
        }

        Prompt saved = promptRepository.save(prompt);
        log.info("Updated prompt id={}, title='{}'", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    @Override
    @Auditable(operation = "DELETE", entityType = "Prompt")
    public void delete(Long id) {
        Prompt prompt = getEntityById(id);
        promptRepository.delete(prompt);
        log.info("Deleted prompt id={}, title='{}'", id, prompt.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromptResponse> list(Pageable pageable) {
        return promptRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromptResponse> listPublished(Pageable pageable) {
        return promptRepository.findByPublishedTrueOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromptResponse> search(String keyword, String source, String model, String tag, Pageable pageable) {
        Specification<Prompt> spec = buildSearchSpec(keyword, source, model, tag);
        return promptRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Prompt getEntityById(Long id) {
        return promptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllTags() {
        return promptRepository.findAllTags();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllModels() {
        return promptRepository.findAllModels();
    }

    private Specification<Prompt> buildSearchSpec(String keyword, String source, String model, String tag) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter to published only for public search
            predicates.add(cb.equal(root.get("published"), true));

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase().trim() + "%"));
            }

            if (source != null && !source.isBlank()) {
                try {
                    PromptSource sourceEnum = PromptSource.valueOf(source.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("source"), sourceEnum));
                } catch (IllegalArgumentException ignored) {
                    // Invalid source value, skip filter
                }
            }

            if (model != null && !model.isBlank()) {
                predicates.add(cb.equal(root.get("model"), model.trim()));
            }

            if (tag != null && !tag.isBlank()) {
                Join<Prompt, String> tagsJoin = root.join("tags");
                predicates.add(cb.equal(tagsJoin, tag.trim()));
                query.distinct(true);
            }

            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PromptResponse toResponse(Prompt prompt) {
        String contentHtml = markdownService.renderToHtml(prompt.getContent());
        String notesHtml = prompt.getNotes() != null ? markdownService.renderToHtml(prompt.getNotes()) : null;
        return PromptResponse.fromEntity(prompt, contentHtml, notesHtml);
    }
}
