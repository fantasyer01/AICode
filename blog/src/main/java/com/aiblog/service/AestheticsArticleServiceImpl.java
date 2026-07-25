package com.aiblog.service;

import com.aiblog.audit.Auditable;
import com.aiblog.dto.AestheticsArticleCreateRequest;
import com.aiblog.dto.AestheticsArticleResponse;
import com.aiblog.dto.AestheticsArticleUpdateRequest;
import com.aiblog.exception.ResourceNotFoundException;
import com.aiblog.model.AestheticsArticle;
import com.aiblog.model.AestheticsDimension;
import com.aiblog.repository.AestheticsArticleRepository;
import com.aiblog.repository.AestheticsDimensionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;

@Service
@Transactional
public class AestheticsArticleServiceImpl implements AestheticsArticleService {

    private static final Logger log = LoggerFactory.getLogger(AestheticsArticleServiceImpl.class);

    private final AestheticsArticleRepository articleRepository;
    private final AestheticsDimensionRepository dimensionRepository;
    private final MarkdownService markdownService;
    private final ImageStorageService imageStorageService;

    public AestheticsArticleServiceImpl(AestheticsArticleRepository articleRepository,
                                        AestheticsDimensionRepository dimensionRepository,
                                        MarkdownService markdownService,
                                        ImageStorageService imageStorageService) {
        this.articleRepository = articleRepository;
        this.dimensionRepository = dimensionRepository;
        this.markdownService = markdownService;
        this.imageStorageService = imageStorageService;
    }

    @Override
    @Auditable(operation = "INSERT", entityType = "AestheticsArticle")
    public AestheticsArticleResponse create(AestheticsArticleCreateRequest request) {
        AestheticsDimension sensory = dimensionRepository.findById(request.getSensoryDimensionId())
                .orElseThrow(() -> new ResourceNotFoundException("AestheticsDimension", request.getSensoryDimensionId()));
        AestheticsDimension domain = dimensionRepository.findById(request.getDomainDimensionId())
                .orElseThrow(() -> new ResourceNotFoundException("AestheticsDimension", request.getDomainDimensionId()));

        AestheticsArticle article = new AestheticsArticle();
        article.setTitle(request.getTitle());
        article.setAuthor(request.getAuthor());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        article.setCoverImageUrl(processImage(request.getCoverImage()));
        article.setSensoryDimension(sensory);
        article.setDomainDimension(domain);
        article.setPublished(request.getPublished() != null ? request.getPublished() : true);

        AestheticsArticle saved = articleRepository.save(article);
        log.info("Created aesthetics article id={}, title='{}'", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AestheticsArticleResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Override
    @Auditable(operation = "UPDATE", entityType = "AestheticsArticle")
    public AestheticsArticleResponse update(Long id, AestheticsArticleUpdateRequest request) {
        AestheticsArticle article = getEntityById(id);

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
        if (request.getCoverImage() != null) {
            article.setCoverImageUrl(processImage(request.getCoverImage()));
        }
        if (request.getSensoryDimensionId() != null) {
            AestheticsDimension sensory = dimensionRepository.findById(request.getSensoryDimensionId())
                    .orElseThrow(() -> new ResourceNotFoundException("AestheticsDimension", request.getSensoryDimensionId()));
            article.setSensoryDimension(sensory);
        }
        if (request.getDomainDimensionId() != null) {
            AestheticsDimension domain = dimensionRepository.findById(request.getDomainDimensionId())
                    .orElseThrow(() -> new ResourceNotFoundException("AestheticsDimension", request.getDomainDimensionId()));
            article.setDomainDimension(domain);
        }
        if (request.getPublished() != null) {
            article.setPublished(request.getPublished());
        }

        AestheticsArticle saved = articleRepository.save(article);
        log.info("Updated aesthetics article id={}, title='{}'", saved.getId(), saved.getTitle());
        return toResponse(saved);
    }

    @Override
    @Auditable(operation = "DELETE", entityType = "AestheticsArticle")
    public void delete(Long id) {
        AestheticsArticle article = getEntityById(id);
        articleRepository.delete(article);
        log.info("Deleted aesthetics article id={}, title='{}'", id, article.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AestheticsArticleResponse> list(Pageable pageable) {
        return articleRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AestheticsArticleResponse> listPublished(Pageable pageable) {
        return articleRepository.findByPublishedTrueOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AestheticsArticleResponse> listPublishedBySensory(Long sensoryDimensionId, Pageable pageable) {
        return articleRepository.findBySensoryDimensionIdAndPublishedTrueOrderByCreatedAtDesc(sensoryDimensionId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AestheticsArticleResponse> listPublishedByDomain(Long domainDimensionId, Pageable pageable) {
        return articleRepository.findByDomainDimensionIdAndPublishedTrueOrderByCreatedAtDesc(domainDimensionId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AestheticsArticleResponse> listPublishedByDimensions(Long sensoryId, Long domainId, Pageable pageable) {
        return articleRepository.findBySensoryDimensionIdAndDomainDimensionIdAndPublishedTrueOrderByCreatedAtDesc(
                sensoryId, domainId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AestheticsArticleResponse> listPublishedByTag(String tag, Pageable pageable) {
        return articleRepository.findByTagAndPublishedTrue(tag, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AestheticsArticle getEntityById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AestheticsArticle", id));
    }

    private AestheticsArticleResponse toResponse(AestheticsArticle article) {
        String contentHtml = markdownService.renderToHtml(article.getContent());
        return AestheticsArticleResponse.fromEntity(article, contentHtml);
    }

    private String processImage(String coverImage) {
        if (coverImage == null || coverImage.isBlank()) {
            return null;
        }
        if (imageStorageService.isBase64Image(coverImage)) {
            try {
                return imageStorageService.saveBase64Image(coverImage);
            } catch (IOException e) {
                log.error("Failed to save cover image: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to save cover image", e);
            }
        }
        return coverImage;
    }
}
