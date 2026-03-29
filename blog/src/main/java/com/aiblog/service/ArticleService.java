package com.aiblog.service;

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

    Page<ArticleResponse> list(Pageable pageable);

    Page<ArticleResponse> listByTag(String tag, Pageable pageable);

    Page<ArticleResponse> listByCategory(String category, Pageable pageable);

    Page<ArticleResponse> listPublished(Pageable pageable);

    Page<ArticleResponse> listPublishedByTag(String tag, Pageable pageable);

    Page<ArticleResponse> listPublishedByCategory(String category, Pageable pageable);

    Article getEntityById(Long id);
}
