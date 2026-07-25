package com.aiblog.service;

import com.aiblog.audit.Auditable;
import com.aiblog.dto.ArticleCreateRequest;
import com.aiblog.dto.ArticleImagePatchRequest;
import com.aiblog.dto.ArticleResponse;
import com.aiblog.dto.ArticleUpdateRequest;
import com.aiblog.exception.ResourceNotFoundException;
import com.aiblog.model.Article;
import com.aiblog.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Map;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private static final Logger log = LoggerFactory.getLogger(ArticleServiceImpl.class);

    private final ArticleRepository articleRepository;
    private final MarkdownService markdownService;

    public ArticleServiceImpl(ArticleRepository articleRepository, MarkdownService markdownService) {
        this.articleRepository = articleRepository;
        this.markdownService = markdownService;
    }

    @Override
    @Auditable(operation = "INSERT", entityType = "Article")
    public ArticleResponse create(ArticleCreateRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setAuthor(request.getAuthor());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        article.setCategory(request.getCategory());
        article.setCoverImageUrl(request.getCoverImageUrl());
        article.setPublished(request.getPublished() != null ? request.getPublished() : true);

        Article saved = articleRepository.save(article);
        log.info("Created article id={}, title='{}'", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponse getById(Long id) {
        Article article = getEntityById(id);
        return toResponse(article);
    }

    @Override
    @Auditable(operation = "UPDATE", entityType = "Article")
    public ArticleResponse update(Long id, ArticleUpdateRequest request) {
        Article article = getEntityById(id);

        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            article.setAuthor(request.getAuthor());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            article.setSummary(request.getSummary());
        }
        if (request.getTags() != null) {
            article.setTags(request.getTags());
        }
        // Category is always updated (allows clearing by setting empty string)
        article.setCategory(request.getCategory());
        if (request.getCoverImageUrl() != null) {
            article.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getPublished() != null) {
            article.setPublished(request.getPublished());
        }

        Article saved = articleRepository.save(article);
        log.info("Updated article id={}, title='{}'", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    @Override
    @Auditable(operation = "DELETE", entityType = "Article")
    public void delete(Long id) {
        Article article = getEntityById(id);
        articleRepository.delete(article);
        log.info("Deleted article id={}, title='{}'", id, article.getTitle());
    }

    @Override
    @Auditable(operation = "UPDATE", entityType = "Article")
    public ArticleResponse patchImages(Long id, ArticleImagePatchRequest request) {
        Article article = getEntityById(id);

        if (request.getCoverImageUrl() != null && !request.getCoverImageUrl().isBlank()) {
            String previous = article.getCoverImageUrl();
            article.setCoverImageUrl(request.getCoverImageUrl());
            log.info("Patched cover image for article id={}: '{}' -> '{}'", id, previous, request.getCoverImageUrl());
        }

        Map<String, String> replacements = request.getContentReplacements();
        if (replacements != null && !replacements.isEmpty()) {
            String content = article.getContent();
            if (content != null) {
                for (Map.Entry<String, String> entry : replacements.entrySet()) {
                    content = content.replace(entry.getKey(), entry.getValue());
                }
                article.setContent(content);
            }
            log.info("Applied {} content replacement(s) for article id={}", replacements.size(), id);
        }

        Article saved = articleRepository.save(article);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> list(Pageable pageable) {
        return articleRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> listByTag(String tag, Pageable pageable) {
        return articleRepository.findByTag(tag, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> listByCategory(String category, Pageable pageable) {
        return articleRepository.findByCategoryOrderByCreatedAtDesc(category, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> listPublished(Pageable pageable) {
        return articleRepository.findByPublishedTrueOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> listPublishedByTag(String tag, Pageable pageable) {
        return articleRepository.findByTagAndPublishedTrue(tag, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> listPublishedByCategory(String category, Pageable pageable) {
        return articleRepository.findByCategoryAndPublishedTrueOrderByCreatedAtDesc(category, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Article getEntityById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", id));
    }

    private ArticleResponse toResponse(Article article) {
        String contentHtml = markdownService.renderToHtml(article.getContent());
        return ArticleResponse.fromEntity(article, contentHtml);
    }
}
