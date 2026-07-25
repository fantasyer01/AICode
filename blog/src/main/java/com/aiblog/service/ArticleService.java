package com.aiblog.service;

import com.aiblog.dto.ArticleCreateRequest;
import com.aiblog.dto.ArticleImagePatchRequest;
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
     * Patches image references on an existing article without replacing other fields.
     *
     * <ul>
     *   <li>If {@code request.coverImageUrl} is non-blank, the article's cover image URL
     *       is replaced unconditionally.</li>
     *   <li>If {@code request.contentReplacements} is non-empty, each map key found in
     *       the article's Markdown content is replaced with its corresponding value.
     *       Keys absent from the content are silently ignored.</li>
     * </ul>
     *
     * @param id      article id
     * @param request patch payload; at least one field must be non-null/non-empty
     * @return updated article response
     */
    ArticleResponse patchImages(Long id, ArticleImagePatchRequest request);

    Page<ArticleResponse> list(Pageable pageable);

    Page<ArticleResponse> listByTag(String tag, Pageable pageable);

    Page<ArticleResponse> listByCategory(String category, Pageable pageable);

    Page<ArticleResponse> listPublished(Pageable pageable);

    Page<ArticleResponse> listPublishedByTag(String tag, Pageable pageable);

    Page<ArticleResponse> listPublishedByCategory(String category, Pageable pageable);

    Article getEntityById(Long id);
}
