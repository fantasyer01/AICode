package com.aiblog.service;

import com.aiblog.dto.AestheticsArticleCreateRequest;
import com.aiblog.dto.AestheticsArticleResponse;
import com.aiblog.dto.AestheticsArticleUpdateRequest;
import com.aiblog.model.AestheticsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AestheticsArticleService {

    AestheticsArticleResponse create(AestheticsArticleCreateRequest request);

    AestheticsArticleResponse getById(Long id);

    AestheticsArticleResponse update(Long id, AestheticsArticleUpdateRequest request);

    void delete(Long id);

    Page<AestheticsArticleResponse> list(Pageable pageable);

    Page<AestheticsArticleResponse> listPublished(Pageable pageable);

    Page<AestheticsArticleResponse> listPublishedBySensory(Long sensoryDimensionId, Pageable pageable);

    Page<AestheticsArticleResponse> listPublishedByDomain(Long domainDimensionId, Pageable pageable);

    Page<AestheticsArticleResponse> listPublishedByDimensions(Long sensoryId, Long domainId, Pageable pageable);

    Page<AestheticsArticleResponse> listPublishedByTag(String tag, Pageable pageable);

    AestheticsArticle getEntityById(Long id);
}
