package com.aiblog.service;

import com.aiblog.dto.ArticleCoverImageUpdateRequest;
import com.aiblog.dto.ArticleCreateRequest;
import com.aiblog.dto.ArticleResponse;
import com.aiblog.dto.ArticleUpdateRequest;
import com.aiblog.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleService {

    ArticleResponse create(ArticleCreateRequest request);

    ArticleResponse getById(Long id);

    ArticleResponse update(Long id, ArticleUpdateRequest request);

    void delete(Long id);

    /**
     * Replaces (or sets, if absent) the cover image of an article.
     * The previous cover image URL is overwritten on the article record;
     * the underlying file on disk is left as-is.
     *
     * @param id      article id
     * @param request transport-agnostic payload carrying raw image bytes + metadata
     * @return updated article response
     */
    ArticleResponse updateCoverImage(Long id, ArticleCoverImageUpdateRequest request);

    Page<ArticleResponse> list(Pageable pageable);

    Page<ArticleResponse> listByTag(String tag, Pageable pageable);

    Page<ArticleResponse> listByCategory(String category, Pageable pageable);

    Page<ArticleResponse> listPublished(Pageable pageable);

    Page<ArticleResponse> listPublishedByTag(String tag, Pageable pageable);

    Page<ArticleResponse> listPublishedByCategory(String category, Pageable pageable);

    Article getEntityById(Long id);
}
